package org.ganjp.blog.rubi.service;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ganjp.blog.common.exception.BusinessException;
import org.ganjp.blog.common.exception.ResourceNotFoundException;
import org.ganjp.blog.rubi.config.RubiProperties;
import org.ganjp.blog.rubi.model.dto.CreateExpressionRuRequest;
import org.ganjp.blog.rubi.model.dto.UpdateExpressionRuRequest;
import org.ganjp.blog.rubi.model.dto.ExpressionRuResponse;
import org.ganjp.blog.rubi.model.entity.ExpressionRu;
import org.ganjp.blog.rubi.repository.ExpressionRuRepository;
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
public class ExpressionRuService {

    private final ExpressionRuRepository expressionRepository;
    private final RubiProperties rubiProperties;

    /**
     * Create a new expression
     */
    @Transactional
    public ExpressionRuResponse createExpression(CreateExpressionRuRequest request, String createdBy) {
        if (expressionRepository.existsByNameAndLang(request.getName(), request.getLang())) {
            throw new BusinessException("Expression already exists for this language: " + request.getName());
        }

        ExpressionRu dbExpression = ExpressionRu.builder()
                .id(UUID.randomUUID().toString())
                .name(request.getName())
                .phonetic(request.getPhonetic())
                .translation(request.getTranslation())
                .explanation(request.getExplanation())
                .example(request.getExample())
                .term(request.getTerm())
                .week(request.getWeek())
                .tags(request.getTags())
                .difficultyLevel(request.getDifficultyLevel())
                .lang(request.getLang())
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        dbExpression.setCreatedBy(createdBy);
        dbExpression.setUpdatedBy(createdBy);

        // Handle audio file upload
        handleAudioUpload(dbExpression, request.getPhoneticAudioFile(), request.getPhoneticAudioOriginalUrl(), request.getPhoneticAudioFilename());

        ExpressionRu savedExpression = expressionRepository.save(dbExpression);
        return ExpressionRuResponse.fromEntity(savedExpression, rubiProperties.getExpression().getBaseUrl());
    }

    /**
     * Update an existing expression
     */
    @Transactional
    public ExpressionRuResponse updateExpression(String id, UpdateExpressionRuRequest request, String updatedBy) {
        ExpressionRu dbExpression = expressionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expression not found with id: " + id));

        if (request.getName() != null && !request.getName().equals(dbExpression.getName())) {
            if (expressionRepository.existsByNameAndLang(request.getName(), 
                    request.getLang() != null ? request.getLang() : dbExpression.getLang())) {
                throw new BusinessException("Expression already exists: " + request.getName());
            }
            dbExpression.setName(request.getName());
        }

        if (request.getPhonetic() != null) dbExpression.setPhonetic(request.getPhonetic());
        if (request.getTranslation() != null) dbExpression.setTranslation(request.getTranslation());
        if (request.getExplanation() != null) dbExpression.setExplanation(request.getExplanation());
        if (request.getExample() != null) dbExpression.setExample(request.getExample());
        if (request.getTerm() != null) dbExpression.setTerm(request.getTerm());
        if (request.getWeek() != null) dbExpression.setWeek(request.getWeek());
        if (request.getTags() != null) dbExpression.setTags(request.getTags());
        if (request.getDifficultyLevel() != null) dbExpression.setDifficultyLevel(request.getDifficultyLevel());
        if (request.getLang() != null) dbExpression.setLang(request.getLang());
        if (request.getDisplayOrder() != null) dbExpression.setDisplayOrder(request.getDisplayOrder());
        if (request.getIsActive() != null) dbExpression.setIsActive(request.getIsActive());

        // Handle audio file upload
        handleAudioUpload(dbExpression, request.getPhoneticAudioFile(), request.getPhoneticAudioOriginalUrl(), request.getPhoneticAudioFilename());

        dbExpression.setUpdatedBy(updatedBy);

        ExpressionRu updatedExpression = expressionRepository.save(dbExpression);
        return ExpressionRuResponse.fromEntity(updatedExpression, rubiProperties.getExpression().getBaseUrl());
    }

    /**
     * Get expression by ID
     */
    public ExpressionRuResponse getExpressionById(String id) {
        ExpressionRu expression = expressionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expression not found with id: " + id));
        return ExpressionRuResponse.fromEntity(expression, rubiProperties.getExpression().getBaseUrl());
    }

    /**
     * Delete expression (Logic delete)
     */
    @Transactional
    public void deleteExpression(String id, String updatedBy) {
        ExpressionRu expression = expressionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expression not found with id: " + id));
        
        expression.setIsActive(false);
        expression.setUpdatedBy(updatedBy);
        expressionRepository.save(expression);
    }

    /**
     * Get expressions with filtering
     */
    public Page<ExpressionRuResponse> getExpressions(String name, ExpressionRu.Language lang, String tags, Boolean isActive, Integer term, Integer week, String difficultyLevel, Pageable pageable) {
        Specification<ExpressionRu> spec = (root, query, cb) -> {
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

        return expressionRepository.findAll(spec, pageable).map(expression -> ExpressionRuResponse.fromEntity(expression, rubiProperties.getExpression().getBaseUrl()));
    }
    
    /**
     * Get active expressions by language
     */
    public List<ExpressionRuResponse> getExpressionsByLanguage(ExpressionRu.Language lang) {
        return expressionRepository.findByLangAndIsActiveTrueOrderByDisplayOrderAsc(lang)
                .stream()
                .map(ExpressionRuResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Handle audio file upload for expression phonetic audio
     */
    private void handleAudioUpload(ExpressionRu expression, MultipartFile newFile, String newOriginalUrl, String newProvidedFilename) {
        String audioDir = rubiProperties.getExpression().getAudio().getDirectory();

        String baseName = StringUtils.hasText(newProvidedFilename) ? newProvidedFilename : expression.getName();
        baseName = baseName.trim().replaceAll("\\s+", "-").toLowerCase();

        if (newFile != null && !newFile.isEmpty()) {
            String ext = getFileExtension(newFile.getOriginalFilename());
            if (!StringUtils.hasText(ext)) ext = "mp3";
            String filename = baseName.endsWith("." + ext) ? baseName : baseName + "." + ext;

            Path targetPath = Path.of(audioDir).resolve(filename);
            try {
                Files.createDirectories(targetPath.getParent());
                Files.copy(newFile.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                expression.setPhoneticAudioFilename(filename);
            } catch (IOException e) {
                throw new BusinessException("Failed to save audio file: " + e.getMessage());
            }
        } else if (StringUtils.hasText(newOriginalUrl)) {
            boolean isUrlChanged = !newOriginalUrl.equals(expression.getPhoneticAudioOriginalUrl());
            expression.setPhoneticAudioOriginalUrl(newOriginalUrl);

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
                        expression.setPhoneticAudioFilename(filename);
                    }
                } catch (Exception e) {
                    log.error("Failed to download audio from URL: {}", newOriginalUrl, e);
                    throw new BusinessException("Failed to download audio file: " + e.getMessage());
                }
            } else if (StringUtils.hasText(expression.getPhoneticAudioFilename())) {
                String currentFilename = expression.getPhoneticAudioFilename();
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
                                expression.setPhoneticAudioFilename(newFilename);
                            }
                        } catch (IOException e) {
                            log.error("Failed to rename audio file from {} to {}", currentFilename, newFilename, e);
                        }
                    }
                } else if (StringUtils.hasText(newProvidedFilename)) {
                    expression.setPhoneticAudioFilename(newProvidedFilename);
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
     * Get expression audio file by filename
     */
    public java.io.File getAudioFile(String filename) {
        Path audioPath = Path.of(rubiProperties.getExpression().getAudio().getDirectory());
        Path filePath = audioPath.resolve(filename);
        return filePath.toFile();
    }
}
