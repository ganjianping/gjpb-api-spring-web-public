package org.ganjp.blog.rubi.service;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ganjp.blog.common.exception.BusinessException;
import org.ganjp.blog.common.exception.ResourceNotFoundException;
import org.ganjp.blog.rubi.config.RubiProperties;
import org.ganjp.blog.rubi.model.dto.CreateVocabularyRuRequest;
import org.ganjp.blog.rubi.model.dto.UpdateVocabularyRuRequest;
import org.ganjp.blog.rubi.model.dto.VocabularyRuResponse;
import org.ganjp.blog.rubi.model.entity.VocabularyRu;
import org.ganjp.blog.rubi.repository.VocabularyRuRepository;
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
public class VocabularyRuService {

    private final VocabularyRuRepository vocabularyRepository;
    private final RubiProperties rubiProperties;

    /**
     * Create a new vocabulary
     */
    @Transactional
    public VocabularyRuResponse createVocabulary(CreateVocabularyRuRequest request, String createdBy) {
        if (vocabularyRepository.existsByNameAndLang(request.getName(), request.getLang())) {
            throw new BusinessException("Vocabulary word already exists for this language: " + request.getName());
        }

        VocabularyRu dbVocabulary = VocabularyRu.builder()
                .id(UUID.randomUUID().toString())
                .name(request.getName())
                .nounPluralForm(request.getNounPluralForm())
                .nounForm(request.getNounForm())
                .nounMeaning(request.getNounMeaning())
                .nounExample(request.getNounExample())
                .verbSimplePastTense(request.getVerbSimplePastTense())
                .verbPastPerfectTense(request.getVerbPastPerfectTense())
                .verbPresentParticiple(request.getVerbPresentParticiple())
                .adjectiveComparativeForm(request.getAdjectiveComparativeForm())
                .adjectiveSuperlativeForm(request.getAdjectiveSuperlativeForm())
                .verbForm(request.getVerbForm())
                .verbMeaning(request.getVerbMeaning())
                .verbExample(request.getVerbExample())
                .adjectiveForm(request.getAdjectiveForm())
                .adjectiveMeaning(request.getAdjectiveMeaning())
                .adjectiveExample(request.getAdjectiveExample())
                .adverbForm(request.getAdverbForm())
                .adverbMeaning(request.getAdverbMeaning())
                .adverbExample(request.getAdverbExample())
                .translation(request.getTranslation())
                .synonyms(request.getSynonyms())
                .phonetic(request.getPhonetic())
                .partOfSpeech(request.getPartOfSpeech())
                .definition(request.getDefinition())
                .example(request.getExample())
                .dictionaryUrl(request.getDictionaryUrl())
                .term(request.getTerm())
                .week(request.getWeek())
                .tags(request.getTags())
                .difficultyLevel(request.getDifficultyLevel())
                .lang(request.getLang())
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        // Handle Image Upload
        handleImageUpload(dbVocabulary, request.getImageFile(), request.getImageOriginalUrl(), request.getImageFilename());

        // Handle Audio Upload
        handleAudioUpload(dbVocabulary, request.getPhoneticAudioFile(), request.getPhoneticAudioOriginalUrl(), request.getPhoneticAudioFilename());

        dbVocabulary.setCreatedBy(createdBy);
        dbVocabulary.setUpdatedBy(createdBy);

        VocabularyRu savedVocabulary = vocabularyRepository.save(dbVocabulary);
        return VocabularyRuResponse.fromEntity(savedVocabulary, rubiProperties.getVocabulary().getBaseUrl());
    }

    /**
     * Update an existing vocabulary
     */
    @Transactional
    public VocabularyRuResponse updateVocabulary(String id, UpdateVocabularyRuRequest request, String updatedBy) {
        VocabularyRu dbVocabulary = vocabularyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vocabulary not found with id: " + id));

        if (request.getName() != null && !request.getName().equals(dbVocabulary.getName())) {
            if (vocabularyRepository.existsByNameAndLang(request.getName(), 
                    request.getLang() != null ? request.getLang() : dbVocabulary.getLang())) {
                throw new BusinessException("Vocabulary word already exists: " + request.getName());
            }
            dbVocabulary.setName(request.getName());
        }

        // Handle Image Upload/Update
        if (request.getImageFile() != null || request.getImageOriginalUrl() != null) {
             handleImageUpload(dbVocabulary, request.getImageFile(), request.getImageOriginalUrl(), request.getImageFilename());
        } else if (request.getImageFilename() != null) {
            dbVocabulary.setImageFilename(request.getImageFilename());
        }

        // Handle Audio Upload/Update
        if (request.getPhoneticAudioFile() != null || request.getPhoneticAudioOriginalUrl() != null) {
            handleAudioUpload(dbVocabulary, request.getPhoneticAudioFile(), request.getPhoneticAudioOriginalUrl(), request.getPhoneticAudioFilename());
        } else if (request.getPhoneticAudioFilename() != null) {
            dbVocabulary.setPhoneticAudioFilename(request.getPhoneticAudioFilename());
        }

        if (request.getNounPluralForm() != null) dbVocabulary.setNounPluralForm(request.getNounPluralForm());
        if (request.getNounForm() != null) dbVocabulary.setNounForm(request.getNounForm());
        if (request.getNounMeaning() != null) dbVocabulary.setNounMeaning(request.getNounMeaning());
        if (request.getNounExample() != null) dbVocabulary.setNounExample(request.getNounExample());
        if (request.getVerbSimplePastTense() != null) dbVocabulary.setVerbSimplePastTense(request.getVerbSimplePastTense());
        if (request.getVerbPastPerfectTense() != null) dbVocabulary.setVerbPastPerfectTense(request.getVerbPastPerfectTense());
        if (request.getVerbPresentParticiple() != null) dbVocabulary.setVerbPresentParticiple(request.getVerbPresentParticiple());
        if (request.getAdjectiveComparativeForm() != null) dbVocabulary.setAdjectiveComparativeForm(request.getAdjectiveComparativeForm());
        if (request.getAdjectiveSuperlativeForm() != null) dbVocabulary.setAdjectiveSuperlativeForm(request.getAdjectiveSuperlativeForm());
        if (request.getVerbForm() != null) dbVocabulary.setVerbForm(request.getVerbForm());
        if (request.getVerbMeaning() != null) dbVocabulary.setVerbMeaning(request.getVerbMeaning());
        if (request.getVerbExample() != null) dbVocabulary.setVerbExample(request.getVerbExample());
        if (request.getAdjectiveForm() != null) dbVocabulary.setAdjectiveForm(request.getAdjectiveForm());
        if (request.getAdjectiveMeaning() != null) dbVocabulary.setAdjectiveMeaning(request.getAdjectiveMeaning());
        if (request.getAdjectiveExample() != null) dbVocabulary.setAdjectiveExample(request.getAdjectiveExample());
        if (request.getAdverbForm() != null) dbVocabulary.setAdverbForm(request.getAdverbForm());
        if (request.getAdverbMeaning() != null) dbVocabulary.setAdverbMeaning(request.getAdverbMeaning());
        if (request.getAdverbExample() != null) dbVocabulary.setAdverbExample(request.getAdverbExample());
        if (request.getTranslation() != null) dbVocabulary.setTranslation(request.getTranslation());
        if (request.getSynonyms() != null) dbVocabulary.setSynonyms(request.getSynonyms());
        if (request.getPhonetic() != null) dbVocabulary.setPhonetic(request.getPhonetic());
        if (request.getPartOfSpeech() != null) dbVocabulary.setPartOfSpeech(request.getPartOfSpeech());
        if (request.getDefinition() != null) dbVocabulary.setDefinition(request.getDefinition());
        if (request.getExample() != null) dbVocabulary.setExample(request.getExample());
        if (request.getDictionaryUrl() != null) dbVocabulary.setDictionaryUrl(request.getDictionaryUrl());
        if (request.getTerm() != null) dbVocabulary.setTerm(request.getTerm());
        if (request.getWeek() != null) dbVocabulary.setWeek(request.getWeek());
        if (request.getTags() != null) dbVocabulary.setTags(request.getTags());
        if (request.getDifficultyLevel() != null) dbVocabulary.setDifficultyLevel(request.getDifficultyLevel());
        if (request.getLang() != null) dbVocabulary.setLang(request.getLang());
        if (request.getDisplayOrder() != null) dbVocabulary.setDisplayOrder(request.getDisplayOrder());
        if (request.getIsActive() != null) dbVocabulary.setIsActive(request.getIsActive());

        dbVocabulary.setUpdatedBy(updatedBy);

        VocabularyRu updatedVocabulary = vocabularyRepository.save(dbVocabulary);
        return VocabularyRuResponse.fromEntity(updatedVocabulary, rubiProperties.getVocabulary().getBaseUrl());
    }

    private void handleImageUpload(VocabularyRu vocabulary, MultipartFile file, String originalUrl, String providedFilename) {
        String imageDir = rubiProperties.getVocabulary().getImage().getDirectory();
        Long maxSize = rubiProperties.getVocabulary().getImage().getMaxSize();

        String baseName = StringUtils.hasText(providedFilename) ? providedFilename : vocabulary.getName();
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
                vocabulary.setImageFilename(filename);
            } catch (IOException e) {
                throw new BusinessException("Failed to save image file: " + e.getMessage());
            }
        } else if (StringUtils.hasText(originalUrl)) {
            boolean isUrlChanged = !originalUrl.equals(vocabulary.getImageOriginalUrl());
            vocabulary.setImageOriginalUrl(originalUrl);

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
                    vocabulary.setImageFilename(filename);
                } catch (Exception e) {
                    log.error("Failed to download image from URL: {}", originalUrl, e);
                }
            } else {
                String currentFilename = vocabulary.getImageFilename();
                if (StringUtils.hasText(currentFilename)) {
                    String ext = getFileExtension(currentFilename);
                    String newFilename = baseName.endsWith("." + ext) ? baseName : baseName + "." + ext;
                    if (!newFilename.equals(currentFilename)) {
                        Path oldPath = Path.of(imageDir).resolve(currentFilename);
                        Path newPath = Path.of(imageDir).resolve(newFilename);
                        try {
                            if (Files.exists(oldPath)) {
                                Files.move(oldPath, newPath, StandardCopyOption.REPLACE_EXISTING);
                                vocabulary.setImageFilename(newFilename);
                            }
                        } catch (IOException e) {
                            log.error("Failed to rename image file from {} to {}", currentFilename, newFilename, e);
                        }
                    }
                } else if (StringUtils.hasText(providedFilename)) {
                    vocabulary.setImageFilename(providedFilename);
                }
            }
        }
    }

    private void handleAudioUpload(VocabularyRu dbVocabulary, MultipartFile newFile, String newOriginalUrl, String newProvidedFilename) {
        String audioDir = rubiProperties.getVocabulary().getAudio().getDirectory();

        String baseName = StringUtils.hasText(newProvidedFilename) ? newProvidedFilename : dbVocabulary.getName();
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
     * Get vocabulary audio file by filename
     */
    public java.io.File getAudioFile(String filename) {
        Path audioPath = Path.of(rubiProperties.getVocabulary().getAudio().getDirectory());
        Path filePath = audioPath.resolve(filename);
        return filePath.toFile();
    }

    /**
     * Get vocabulary image file by filename
     */
    public java.io.File getImageFile(String filename) {
        Path imagePath = Path.of(rubiProperties.getVocabulary().getImage().getDirectory());
        Path filePath = imagePath.resolve(filename);
        return filePath.toFile();
    }

    /**
     * Get vocabulary by ID
     */
    public VocabularyRuResponse getVocabularyById(String id) {
        VocabularyRu vocabulary = vocabularyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vocabulary not found with id: " + id));
        return VocabularyRuResponse.fromEntity(vocabulary, rubiProperties.getVocabulary().getBaseUrl());
    }

    /**
     * Delete vocabulary (Logic delete)
     */
    @Transactional
    public void deleteVocabulary(String id, String updatedBy) {
        VocabularyRu vocabulary = vocabularyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vocabulary not found with id: " + id));
        
        vocabulary.setIsActive(false);
        vocabulary.setUpdatedBy(updatedBy);
        vocabularyRepository.save(vocabulary);
    }

    /**
     * Get vocabularies with filtering
     */
    public Page<VocabularyRuResponse> getVocabularies(String word, VocabularyRu.Language lang, String tags, Boolean isActive, Integer term, Integer week, String difficultyLevel, String partOfSpeech, Pageable pageable) {
        Specification<VocabularyRu> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(word)) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + word.toLowerCase() + "%"));
            }

            if (lang != null) {
                predicates.add(cb.equal(root.get("lang"), lang));
            }

            if (StringUtils.hasText(tags)) {
                // Split tags by comma and create AND conditions for each tag
                String[] tagArray = tags.split(",");
                List<Predicate> tagPredicates = new ArrayList<>();
                for (String tag : tagArray) {
                    String trimmedTag = tag.trim();
                    if (StringUtils.hasText(trimmedTag)) {
                        tagPredicates.add(cb.like(root.get("tags"), "%" + trimmedTag + "%"));
                    }
                }
                if (!tagPredicates.isEmpty()) {
                    predicates.add(cb.and(tagPredicates.toArray(new Predicate[0])));
                }
            }

            if (isActive != null) {
                predicates.add(cb.equal(root.get("isActive"), isActive));
            }

            if (term != null) {
                predicates.add(cb.equal(root.get("term"), term));
            }

            if (week != null) {
                predicates.add(cb.equal(root.get("week"), week));
            }

            if (StringUtils.hasText(difficultyLevel)) {
                predicates.add(cb.equal(root.get("difficultyLevel"), difficultyLevel));
            }

            if (StringUtils.hasText(partOfSpeech)) {
                predicates.add(cb.like(cb.lower(root.get("partOfSpeech")), "%" + partOfSpeech.toLowerCase() + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return vocabularyRepository.findAll(spec, pageable).map(vocab -> VocabularyRuResponse.fromEntity(vocab, rubiProperties.getVocabulary().getBaseUrl()));
    }
    
    /**
     * Get active vocabularies by language
     */
    public List<VocabularyRuResponse> getVocabulariesByLanguage(VocabularyRu.Language lang) {
        return vocabularyRepository.findByLangAndIsActiveTrueOrderByDisplayOrderAsc(lang)
                .stream()
                .map(vocab -> VocabularyRuResponse.fromEntity(vocab, rubiProperties.getVocabulary().getBaseUrl()))
                .collect(Collectors.toList());
    }
}
