package org.ganjp.blog.rubi.service;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ganjp.blog.common.exception.BusinessException;
import org.ganjp.blog.common.exception.ResourceNotFoundException;
import org.ganjp.blog.rubi.config.RubiProperties;
import org.ganjp.blog.rubi.model.dto.CreateVocabularyRequest;
import org.ganjp.blog.rubi.model.dto.UpdateVocabularyRequest;
import org.ganjp.blog.rubi.model.dto.VocabularyResponse;
import org.ganjp.blog.rubi.model.entity.Vocabulary;
import org.ganjp.blog.rubi.repository.VocabularyRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VocabularyService {

    private final VocabularyRepository vocabularyRepository;
    private final RubiProperties rubiProperties;

    /**
     * Create a new vocabulary
     */
    @Transactional
    public VocabularyResponse createVocabulary(CreateVocabularyRequest request, String createdBy) {
        if (vocabularyRepository.existsByWordAndLang(request.getWord(), request.getLang())) {
            throw new BusinessException("Vocabulary word already exists for this language: " + request.getWord());
        }

        Vocabulary dbVocabulary = Vocabulary.builder()
                .id(UUID.randomUUID().toString())
                .word(request.getWord())
                .simplePastTense(request.getSimplePastTense())
                .pastPerfectTense(request.getPastPerfectTense())
                .translation(request.getTranslation())
                .synonyms(request.getSynonyms())
                .pluralForm(request.getPluralForm())
                .phonetic(request.getPhonetic())
                .partOfSpeech(request.getPartOfSpeech())
                .definition(request.getDefinition())
                .example(request.getExample())
                .dictionaryUrl(request.getDictionaryUrl())
                .tags(request.getTags())
                .lang(request.getLang())
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        // Handle Image Upload
        handleImageUpload(dbVocabulary, request.getWordImageFile(), request.getWordImageOriginalUrl(), request.getWordImageFilename());

        // Handle Audio Upload
        handleAudioUpload(dbVocabulary, request.getPhoneticAudioFile(), request.getPhoneticAudioOriginalUrl(), request.getPhoneticAudioFilename());

        dbVocabulary.setCreatedBy(createdBy);
        dbVocabulary.setUpdatedBy(createdBy);

        Vocabulary savedVocabulary = vocabularyRepository.save(dbVocabulary);
        return VocabularyResponse.fromEntity(savedVocabulary);
    }

    /**
     * Update an existing vocabulary
     */
    @Transactional
    public VocabularyResponse updateVocabulary(String id, UpdateVocabularyRequest request, String updatedBy) {
        Vocabulary dbVocabulary = vocabularyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vocabulary not found with id: " + id));

        if (request.getWord() != null && !request.getWord().equals(dbVocabulary.getWord())) {
            if (vocabularyRepository.existsByWordAndLang(request.getWord(), 
                    request.getLang() != null ? request.getLang() : dbVocabulary.getLang())) {
                throw new BusinessException("Vocabulary word already exists: " + request.getWord());
            }
            dbVocabulary.setWord(request.getWord());
        }

        // Handle Image Upload/Update
        if (request.getWordImageFile() != null || request.getWordImageOriginalUrl() != null) {
             handleImageUpload(dbVocabulary, request.getWordImageFile(), request.getWordImageOriginalUrl(), request.getWordImageFilename());
        } else if (request.getWordImageFilename() != null) {
            dbVocabulary.setWordImageFilename(request.getWordImageFilename());
        }

        // Handle Audio Upload/Update
        if (request.getPhoneticAudioFile() != null || request.getPhoneticAudioOriginalUrl() != null) {
            handleAudioUpload(dbVocabulary, request.getPhoneticAudioFile(), request.getPhoneticAudioOriginalUrl(), request.getPhoneticAudioFilename());
        } else if (request.getPhoneticAudioFilename() != null) {
            dbVocabulary.setPhoneticAudioFilename(request.getPhoneticAudioFilename());
        }

        if (request.getSimplePastTense() != null) dbVocabulary.setSimplePastTense(request.getSimplePastTense());
        if (request.getPastPerfectTense() != null) dbVocabulary.setPastPerfectTense(request.getPastPerfectTense());
        if (request.getTranslation() != null) dbVocabulary.setTranslation(request.getTranslation());
        if (request.getSynonyms() != null) dbVocabulary.setSynonyms(request.getSynonyms());
        if (request.getPluralForm() != null) dbVocabulary.setPluralForm(request.getPluralForm());
        if (request.getPhonetic() != null) dbVocabulary.setPhonetic(request.getPhonetic());
        if (request.getPartOfSpeech() != null) dbVocabulary.setPartOfSpeech(request.getPartOfSpeech());
        if (request.getDefinition() != null) dbVocabulary.setDefinition(request.getDefinition());
        if (request.getExample() != null) dbVocabulary.setExample(request.getExample());
        if (request.getDictionaryUrl() != null) dbVocabulary.setDictionaryUrl(request.getDictionaryUrl());
        if (request.getTags() != null) dbVocabulary.setTags(request.getTags());
        if (request.getLang() != null) dbVocabulary.setLang(request.getLang());
        if (request.getDisplayOrder() != null) dbVocabulary.setDisplayOrder(request.getDisplayOrder());
        if (request.getIsActive() != null) dbVocabulary.setIsActive(request.getIsActive());

        dbVocabulary.setUpdatedBy(updatedBy);

        Vocabulary updatedVocabulary = vocabularyRepository.save(dbVocabulary);
        return VocabularyResponse.fromEntity(updatedVocabulary);
    }

    private void handleImageUpload(Vocabulary vocabulary, MultipartFile file, String originalUrl, String providedFilename) {
        String imageDir = rubiProperties.getVocabulary().getImage().getDirectory();
        Long maxSize = rubiProperties.getVocabulary().getImage().getMaxSize();

        String baseName = StringUtils.hasText(providedFilename) ? providedFilename : vocabulary.getWord();
        baseName = baseName.trim().replaceAll("\\s+", "-").toLowerCase();

        if (file != null && !file.isEmpty()) {
            String ext = getFileExtension(file.getOriginalFilename());
            if (!StringUtils.hasText(ext)) ext = "png";
            String filename = baseName.endsWith("." + ext) ? baseName : baseName + "." + ext;

            Path targetPath = Path.of(imageDir).resolve(filename);
            try {
                Files.createDirectories(targetPath.getParent());
                
                BufferedImage original = ImageIO.read(file.getInputStream());
                if (original != null && maxSize != null) {
                    BufferedImage resized = resizeImageIfNeeded(original, maxSize.intValue());
                    ImageIO.write(resized, ext, targetPath.toFile());
                } else {
                    Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                }
                vocabulary.setWordImageFilename(filename);
            } catch (IOException e) {
                throw new BusinessException("Failed to save image file: " + e.getMessage());
            }
        } else if (StringUtils.hasText(originalUrl)) {
            boolean isUrlChanged = !originalUrl.equals(vocabulary.getWordImageOriginalUrl());
            vocabulary.setWordImageOriginalUrl(originalUrl);

            if (isUrlChanged && originalUrl.toLowerCase().startsWith("http")) {
                try {
                    java.net.URL url = new java.net.URL(originalUrl);
                    String ext = getFileExtension(url.getPath());
                    if (!StringUtils.hasText(ext)) ext = "jpg";
                    String filename = baseName.endsWith("." + ext) ? baseName : baseName + "." + ext;
                    Path targetPath = Path.of(imageDir).resolve(filename);
                    Files.createDirectories(targetPath.getParent());

                    try (java.io.InputStream in = url.openStream()) {
                        BufferedImage original = ImageIO.read(in);
                        if (original != null && maxSize != null) {
                            BufferedImage resized = resizeImageIfNeeded(original, maxSize.intValue());
                            ImageIO.write(resized, ext, targetPath.toFile());
                        } else {
                            try (java.io.InputStream in2 = url.openStream()) {
                                Files.copy(in2, targetPath, StandardCopyOption.REPLACE_EXISTING);
                            }
                        }
                    }
                    vocabulary.setWordImageFilename(filename);
                } catch (Exception e) {
                    log.error("Failed to download image from URL: {}", originalUrl, e);
                }
            } else {
                String currentFilename = vocabulary.getWordImageFilename();
                if (StringUtils.hasText(currentFilename)) {
                    String ext = getFileExtension(currentFilename);
                    String newFilename = baseName.endsWith("." + ext) ? baseName : baseName + "." + ext;
                    if (!newFilename.equals(currentFilename)) {
                        Path oldPath = Path.of(imageDir).resolve(currentFilename);
                        Path newPath = Path.of(imageDir).resolve(newFilename);
                        try {
                            if (Files.exists(oldPath)) {
                                Files.move(oldPath, newPath, StandardCopyOption.REPLACE_EXISTING);
                                vocabulary.setWordImageFilename(newFilename);
                            }
                        } catch (IOException e) {
                            log.error("Failed to rename image file from {} to {}", currentFilename, newFilename, e);
                        }
                    }
                } else if (StringUtils.hasText(providedFilename)) {
                    vocabulary.setWordImageFilename(providedFilename);
                }
            }
        }
    }

    private void handleAudioUpload(Vocabulary dbVocabulary, MultipartFile newFile, String newOriginalUrl, String newProvidedFilename) {
        String audioDir = rubiProperties.getVocabulary().getAudio().getDirectory();

        String baseName = StringUtils.hasText(newProvidedFilename) ? newProvidedFilename : dbVocabulary.getWord();
        baseName = baseName.trim().replaceAll("\\s+", "-").toLowerCase();

        if (newFile != null && !newFile.isEmpty()) {
            String ext = getFileExtension(newFile.getOriginalFilename());
            if (!StringUtils.hasText(ext)) ext = "mp3";
            String filename = baseName.endsWith("." + ext) ? baseName : baseName + "." + ext;

            Path targetPath = Path.of(audioDir).resolve(filename);
            try {
                Files.createDirectories(targetPath.getParent());
                Files.copy(newFile.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                dbVocabulary.setPhoneticAudioFilename(filename);
            } catch (IOException e) {
                throw new BusinessException("Failed to save audio file: " + e.getMessage());
            }
        } else if (StringUtils.hasText(newOriginalUrl)) {
            boolean isUrlChanged = !newOriginalUrl.equals(dbVocabulary.getPhoneticAudioOriginalUrl());
            dbVocabulary.setPhoneticAudioOriginalUrl(newOriginalUrl);

            if (isUrlChanged && newOriginalUrl.toLowerCase().startsWith("http")) {
                try {
                    java.net.URL url = new java.net.URL(newOriginalUrl);
                    String ext = getFileExtension(url.getPath());
                    if (!StringUtils.hasText(ext)) ext = "mp3";
                    String filename = baseName.endsWith("." + ext) ? baseName : baseName + "." + ext;
                    Path targetPath = Path.of(audioDir).resolve(filename);
                    Files.createDirectories(targetPath.getParent());

                    try (java.io.InputStream in = url.openStream()) {
                        Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    }
                    dbVocabulary.setPhoneticAudioFilename(filename);
                } catch (Exception e) {
                    log.error("Failed to download audio from URL: {}", newOriginalUrl, e);
                }
            } else {
                String currentFilename = dbVocabulary.getPhoneticAudioFilename();
                if (StringUtils.hasText(currentFilename)) {
                    String ext = getFileExtension(currentFilename);
                    String newFilename = baseName.endsWith("." + ext) ? baseName : baseName + "." + ext;
                    if (!newFilename.equals(currentFilename)) {
                        Path oldPath = Path.of(audioDir).resolve(currentFilename);
                        Path newPath = Path.of(audioDir).resolve(newFilename);
                        try {
                            if (Files.exists(oldPath)) {
                                Files.move(oldPath, newPath, StandardCopyOption.REPLACE_EXISTING);
                                dbVocabulary.setPhoneticAudioFilename(newFilename);
                            }
                        } catch (IOException e) {
                            log.error("Failed to rename audio file from {} to {}", currentFilename, newFilename, e);
                        }
                    }
                } else if (StringUtils.hasText(newProvidedFilename)) {
                    dbVocabulary.setPhoneticAudioFilename(newProvidedFilename);
                }
            }
        }
    }

    private BufferedImage resizeImageIfNeeded(BufferedImage original, int maxSize) {
        int width = original.getWidth();
        int height = original.getHeight();

        if (width <= maxSize && height <= maxSize) {
            return original;
        }

        double ratio = (double) width / height;
        if (width > height) {
            width = maxSize;
            height = (int) (width / ratio);
        } else {
            height = maxSize;
            width = (int) (height * ratio);
        }

        BufferedImage resized = new BufferedImage(width, height, original.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : original.getType());
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(original, 0, 0, width, height, null);
        g.dispose();
        return resized;
    }

    private String getFileExtension(String filename) {
        if (filename == null) return "";
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex >= 0 && dotIndex < filename.length() - 1) {
            return filename.substring(dotIndex + 1).toLowerCase();
        }
        return "";
    }

    /**
     * Get vocabulary by ID
     */
    public VocabularyResponse getVocabularyById(String id) {
        Vocabulary vocabulary = vocabularyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vocabulary not found with id: " + id));
        return VocabularyResponse.fromEntity(vocabulary);
    }

    /**
     * Delete vocabulary (Logic delete)
     */
    @Transactional
    public void deleteVocabulary(String id, String updatedBy) {
        Vocabulary vocabulary = vocabularyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vocabulary not found with id: " + id));
        
        vocabulary.setIsActive(false);
        vocabulary.setUpdatedBy(updatedBy);
        vocabularyRepository.save(vocabulary);
    }

    /**
     * Get vocabularies with filtering
     */
    public Page<VocabularyResponse> getVocabularies(String word, Vocabulary.Language lang, String tags, Boolean isActive, Pageable pageable) {
        Specification<Vocabulary> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(word)) {
                predicates.add(cb.like(cb.lower(root.get("word")), "%" + word.toLowerCase() + "%"));
            }

            if (lang != null) {
                predicates.add(cb.equal(root.get("lang"), lang));
            }

            if (StringUtils.hasText(tags)) {
                predicates.add(cb.like(root.get("tags"), "%" + tags + "%"));
            }

            if (isActive != null) {
                predicates.add(cb.equal(root.get("isActive"), isActive));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return vocabularyRepository.findAll(spec, pageable).map(VocabularyResponse::fromEntity);
    }
    
    /**
     * Get active vocabularies by language
     */
    public List<VocabularyResponse> getVocabulariesByLanguage(Vocabulary.Language lang) {
        return vocabularyRepository.findByLangAndIsActiveTrueOrderByDisplayOrderAsc(lang)
                .stream()
                .map(VocabularyResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
