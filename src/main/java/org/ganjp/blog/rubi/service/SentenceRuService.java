package org.ganjp.blog.rubi.service;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ganjp.blog.common.exception.BusinessException;
import org.ganjp.blog.common.exception.ResourceNotFoundException;
import org.ganjp.blog.rubi.config.RubiProperties;
import org.ganjp.blog.rubi.model.dto.CreateSentenceRuRequest;
import org.ganjp.blog.rubi.model.dto.UpdateSentenceRuRequest;
import org.ganjp.blog.rubi.model.dto.SentenceRuResponse;
import org.ganjp.blog.rubi.model.entity.SentenceRu;
import org.ganjp.blog.rubi.repository.SentenceRuRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
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
public class SentenceRuService {

    private final SentenceRuRepository sentenceRepository;
    private final RubiProperties rubiProperties;

    /**
     * Create a new sentence
     */
    @Transactional
    public SentenceRuResponse createSentence(CreateSentenceRuRequest request, String createdBy) {
        if (sentenceRepository.existsByNameAndLang(request.getName(), request.getLang())) {
            throw new BusinessException("Sentence already exists for this language: " + request.getName());
        }

        SentenceRu dbSentence = SentenceRu.builder()
                .id(UUID.randomUUID().toString())
                .name(request.getName())
                .phonetic(request.getPhonetic())
                .translation(request.getTranslation())
                .explanation(request.getExplanation())
                .term(request.getTerm())
                .week(request.getWeek())
                .tags(request.getTags())
                .difficultyLevel(request.getDifficultyLevel())
                .lang(request.getLang())
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        dbSentence.setCreatedBy(createdBy);
        dbSentence.setUpdatedBy(createdBy);

        // Handle audio file upload
        handleAudioUpload(dbSentence, request.getPhoneticAudioFile(), request.getPhoneticAudioOriginalUrl(), request.getPhoneticAudioFilename());

        SentenceRu savedSentence = sentenceRepository.save(dbSentence);
        return SentenceRuResponse.fromEntity(savedSentence, rubiProperties.getSentence().getBaseUrl());
    }

    /**
     * Update an existing sentence
     */
    @Transactional
    public SentenceRuResponse updateSentence(String id, UpdateSentenceRuRequest request, String updatedBy) {
        SentenceRu dbSentence = sentenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sentence not found with id: " + id));

        if (request.getName() != null && !request.getName().equals(dbSentence.getName())) {
            if (sentenceRepository.existsByNameAndLang(request.getName(), 
                    request.getLang() != null ? request.getLang() : dbSentence.getLang())) {
                throw new BusinessException("Sentence already exists: " + request.getName());
            }
            dbSentence.setName(request.getName());
        }

        if (request.getPhonetic() != null) dbSentence.setPhonetic(request.getPhonetic());
        if (request.getTranslation() != null) dbSentence.setTranslation(request.getTranslation());
        if (request.getExplanation() != null) dbSentence.setExplanation(request.getExplanation());
        if (request.getTerm() != null) dbSentence.setTerm(request.getTerm());
        if (request.getWeek() != null) dbSentence.setWeek(request.getWeek());
        if (request.getTags() != null) dbSentence.setTags(request.getTags());
        if (request.getDifficultyLevel() != null) dbSentence.setDifficultyLevel(request.getDifficultyLevel());
        if (request.getLang() != null) dbSentence.setLang(request.getLang());
        if (request.getDisplayOrder() != null) dbSentence.setDisplayOrder(request.getDisplayOrder());
        if (request.getIsActive() != null) dbSentence.setIsActive(request.getIsActive());

        // Handle audio file upload
        handleAudioUpload(dbSentence, request.getPhoneticAudioFile(), request.getPhoneticAudioOriginalUrl(), request.getPhoneticAudioFilename());

        dbSentence.setUpdatedBy(updatedBy);

        SentenceRu updatedSentence = sentenceRepository.save(dbSentence);
        return SentenceRuResponse.fromEntity(updatedSentence, rubiProperties.getSentence().getBaseUrl());
    }

    /**
     * Get sentence by ID
     */
    public SentenceRuResponse getSentenceById(String id) {
        SentenceRu sentence = sentenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sentence not found with id: " + id));
        return SentenceRuResponse.fromEntity(sentence, rubiProperties.getSentence().getBaseUrl());
    }

    /**
     * Delete sentence (Logic delete)
     */
    @Transactional
    public void deleteSentence(String id, String updatedBy) {
        SentenceRu sentence = sentenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sentence not found with id: " + id));
        
        sentence.setIsActive(false);
        sentence.setUpdatedBy(updatedBy);
        sentenceRepository.save(sentence);
    }

    /**
     * Get sentences with filtering
     */
    public Page<SentenceRuResponse> getSentences(String name, SentenceRu.Language lang, String tags, Boolean isActive, Integer term, Integer week, String difficultyLevel, Pageable pageable) {
        Specification<SentenceRu> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(name)) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
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

            if (term != null) {
                predicates.add(cb.equal(root.get("term"), term));
            }

            if (week != null) {
                predicates.add(cb.equal(root.get("week"), week));
            }

            if (StringUtils.hasText(difficultyLevel)) {
                predicates.add(cb.equal(root.get("difficultyLevel"), difficultyLevel));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return sentenceRepository.findAll(spec, pageable).map(sentence -> SentenceRuResponse.fromEntity(sentence, rubiProperties.getSentence().getBaseUrl()));
    }
    
    /**
     * Get active sentences by language
     */
    public List<SentenceRuResponse> getSentencesByLanguage(SentenceRu.Language lang) {
        return sentenceRepository.findByLangAndIsActiveTrueOrderByDisplayOrderAsc(lang)
                .stream()
                .map(sentence -> SentenceRuResponse.fromEntity(sentence, rubiProperties.getSentence().getBaseUrl()))
                .collect(Collectors.toList());
    }

    /**
     * Handle audio file upload for sentence phonetic audio
     */
    private void handleAudioUpload(SentenceRu sentence, MultipartFile newFile, String newOriginalUrl, String newProvidedFilename) {
        String audioDir = rubiProperties.getSentence().getAudio().getDirectory();

        String baseName = StringUtils.hasText(newProvidedFilename) ? newProvidedFilename : sentence.getName();
        baseName = baseName.trim().replaceAll("\\s+", "-").toLowerCase();
        // Limit filename length to avoid filesystem issues
        if (baseName.length() > 100) {
            baseName = baseName.substring(0, 100);
        }

        if (newFile != null && !newFile.isEmpty()) {
            String ext = getFileExtension(newFile.getOriginalFilename());
            if (!StringUtils.hasText(ext)) ext = "mp3";
            String filename = baseName.endsWith("." + ext) ? baseName : baseName + "." + ext;

            Path targetPath = Path.of(audioDir).resolve(filename);
            try {
                Files.createDirectories(targetPath.getParent());
                Files.copy(newFile.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                sentence.setPhoneticAudioFilename(filename);
            } catch (IOException e) {
                throw new BusinessException("Failed to save audio file: " + e.getMessage());
            }
        } else if (StringUtils.hasText(newOriginalUrl)) {
            boolean isUrlChanged = !newOriginalUrl.equals(sentence.getPhoneticAudioOriginalUrl());
            sentence.setPhoneticAudioOriginalUrl(newOriginalUrl);

            if (isUrlChanged && newOriginalUrl.toLowerCase().startsWith("http")) {
                try {
                    java.net.URL url = new java.net.URL(newOriginalUrl);
                    String ext = getFileExtension(url.getPath());
                    if (!StringUtils.hasText(ext)) ext = "mp3";
                    String filename = baseName.endsWith("." + ext) ? baseName : baseName + "." + ext;
                    Path targetPath = Path.of(audioDir).resolve(filename);
                    Files.createDirectories(targetPath.getParent());

                    try (InputStream in = url.openStream()) {
                        Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
                        sentence.setPhoneticAudioFilename(filename);
                    }
                } catch (Exception e) {
                    log.error("Failed to download audio from URL: {}", newOriginalUrl, e);
                    throw new BusinessException("Failed to download audio file: " + e.getMessage());
                }
            } else if (StringUtils.hasText(sentence.getPhoneticAudioFilename())) {
                String currentFilename = sentence.getPhoneticAudioFilename();
                if (StringUtils.hasText(newProvidedFilename)) {
                    String newExt = getFileExtension(currentFilename);
                    if (!StringUtils.hasText(newExt)) newExt = "mp3";
                    String newFilename = baseName.endsWith("." + newExt) ? baseName : baseName + "." + newExt;
                    
                    if (!newFilename.equals(currentFilename)) {
                        Path oldPath = Path.of(audioDir).resolve(currentFilename);
                        Path newPath = Path.of(audioDir).resolve(newFilename);
                        try {
                            if (Files.exists(oldPath)) {
                                Files.move(oldPath, newPath, StandardCopyOption.REPLACE_EXISTING);
                                sentence.setPhoneticAudioFilename(newFilename);
                            }
                        } catch (IOException e) {
                            log.error("Failed to rename audio file from {} to {}", currentFilename, newFilename, e);
                        }
                    }
                } else if (StringUtils.hasText(newProvidedFilename)) {
                    sentence.setPhoneticAudioFilename(newProvidedFilename);
                }
            }
        }
    }

    /**
     * Extract file extension from filename
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) return "";
        int dotIndex = filename.lastIndexOf('.');
        return dotIndex > 0 ? filename.substring(dotIndex + 1) : "";
    }

    /**
     * Get sentence audio file by filename
     */
    public java.io.File getAudioFile(String filename) {
        Path audioPath = Path.of(rubiProperties.getSentence().getAudio().getDirectory());
        Path filePath = audioPath.resolve(filename);
        return filePath.toFile();
    }
}
