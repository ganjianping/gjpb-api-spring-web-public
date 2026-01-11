package org.ganjp.blog.rubi.service;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ganjp.blog.common.exception.ResourceNotFoundException;
import org.ganjp.blog.rubi.model.dto.CreateFillBlankQuestionRuRequest;
import org.ganjp.blog.rubi.model.dto.FillBlankQuestionRuResponse;
import org.ganjp.blog.rubi.model.dto.UpdateFillBlankQuestionRuRequest;
import org.ganjp.blog.rubi.model.entity.FillBlankQuestionRu;
import org.ganjp.blog.rubi.repository.FillBlankQuestionRuRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FillBlankQuestionRuService {

    private final FillBlankQuestionRuRepository fillBlankQuestionRuRepository;

    /**
     * Create a new FillBlankQuestion
     */
    @Transactional
    public FillBlankQuestionRuResponse createFillBlankQuestionRu(CreateFillBlankQuestionRuRequest request, String createdBy) {
        FillBlankQuestionRu fillBlankQuestionRu = FillBlankQuestionRu.builder()
                .id(UUID.randomUUID().toString())
                .question(request.getQuestion())
                .answer(request.getAnswer())
                .explanation(request.getExplanation())
                .difficultyLevel(request.getDifficultyLevel())
                .tags(request.getTags())
                .lang(request.getLang())
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .failCount(0)
                .successCount(0)
                .build();

        FillBlankQuestionRu savedFillBlankQuestionRu = fillBlankQuestionRuRepository.save(fillBlankQuestionRu);
        log.info("Created FillBlankQuestion with id: {}", savedFillBlankQuestionRu.getId());

        return mapToResponse(savedFillBlankQuestionRu);
    }

    /**
     * Get FillBlankQuestion by ID
     */
    public FillBlankQuestionRuResponse getFillBlankQuestionRuById(String id) {
        FillBlankQuestionRu fillBlankQuestionRu = fillBlankQuestionRuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FillBlankQuestion not found with id: " + id));
        return mapToResponse(fillBlankQuestionRu);
    }

    /**
     * Get all FillBlankQuestions with pagination and filtering
     */
    public Page<FillBlankQuestionRuResponse> getAllFillBlankQuestionRus(Pageable pageable, String lang, String difficultyLevel, String tags, Boolean isActive) {
        Specification<FillBlankQuestionRu> spec = buildSpecification(lang, difficultyLevel, tags, isActive);
        Page<FillBlankQuestionRu> fillBlankQuestionRus = fillBlankQuestionRuRepository.findAll(spec, pageable);
        return fillBlankQuestionRus.map(this::mapToResponse);
    }

    /**
     * Update FillBlankQuestion
     */
    @Transactional
    public FillBlankQuestionRuResponse updateFillBlankQuestionRu(String id, UpdateFillBlankQuestionRuRequest request, String updatedBy) {
        FillBlankQuestionRu fillBlankQuestionRu = fillBlankQuestionRuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FillBlankQuestion not found with id: " + id));

        if (StringUtils.hasText(request.getQuestion())) {
            fillBlankQuestionRu.setQuestion(request.getQuestion());
        }
        if (StringUtils.hasText(request.getAnswer())) {
            fillBlankQuestionRu.setAnswer(request.getAnswer());
        }
        if (request.getExplanation() != null) {
            fillBlankQuestionRu.setExplanation(request.getExplanation());
        }
        if (request.getDifficultyLevel() != null) {
            fillBlankQuestionRu.setDifficultyLevel(request.getDifficultyLevel());
        }
        if (request.getTags() != null) {
            fillBlankQuestionRu.setTags(request.getTags());
        }
        if (request.getLang() != null) {
            fillBlankQuestionRu.setLang(request.getLang());
        }
        if (request.getDisplayOrder() != null) {
            fillBlankQuestionRu.setDisplayOrder(request.getDisplayOrder());
        }
        if (request.getIsActive() != null) {
            fillBlankQuestionRu.setIsActive(request.getIsActive());
        }

        fillBlankQuestionRu.setUpdatedBy(updatedBy);

        FillBlankQuestionRu updatedFillBlankQuestionRu = fillBlankQuestionRuRepository.save(fillBlankQuestionRu);
        log.info("Updated FillBlankQuestion with id: {}", updatedFillBlankQuestionRu.getId());

        return mapToResponse(updatedFillBlankQuestionRu);
    }

    /**
     * Delete FillBlankQuestion (soft delete)
     */
    @Transactional
    public void deleteFillBlankQuestionRu(String id, String deletedBy) {
        FillBlankQuestionRu fillBlankQuestionRu = fillBlankQuestionRuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FillBlankQuestion not found with id: " + id));

        fillBlankQuestionRu.setIsActive(false);
        fillBlankQuestionRu.setUpdatedBy(deletedBy);
        fillBlankQuestionRuRepository.save(fillBlankQuestionRu);

        log.info("Soft deleted FillBlankQuestion with id: {}", id);
    }

    /**
     * Permanently delete FillBlankQuestion
     */
    @Transactional
    public void deleteFillBlankQuestionRuPermanently(String id) {
        FillBlankQuestionRu fillBlankQuestionRu = fillBlankQuestionRuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FillBlankQuestion not found with id: " + id));

        fillBlankQuestionRuRepository.delete(fillBlankQuestionRu);
        log.info("Permanently deleted FillBlankQuestion with id: {}", id);
    }

    /**
     * Get active FillBlankQuestions by language
     */
    public List<FillBlankQuestionRuResponse> getActiveFillBlankQuestionRusByLang(FillBlankQuestionRu.Language lang) {
        List<FillBlankQuestionRu> fillBlankQuestionRus = fillBlankQuestionRuRepository.findByLangAndIsActiveTrueOrderByDisplayOrderAsc(lang);
        return fillBlankQuestionRus.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Build specification for filtering
     */
    private Specification<FillBlankQuestionRu> buildSpecification(String lang, String difficultyLevel, String tags, Boolean isActive) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(lang)) {
                predicates.add(criteriaBuilder.equal(root.get("lang"), FillBlankQuestionRu.Language.valueOf(lang.toUpperCase())));
            }

            if (StringUtils.hasText(difficultyLevel)) {
                predicates.add(criteriaBuilder.equal(root.get("difficultyLevel"), difficultyLevel));
            }

            if (StringUtils.hasText(tags)) {
                predicates.add(criteriaBuilder.like(root.get("tags"), "%" + tags + "%"));
            }

            if (isActive != null) {
                predicates.add(criteriaBuilder.equal(root.get("isActive"), isActive));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Map entity to response DTO
     */
    private FillBlankQuestionRuResponse mapToResponse(FillBlankQuestionRu fillBlankQuestionRu) {
        return FillBlankQuestionRuResponse.builder()
                .id(fillBlankQuestionRu.getId())
                .question(fillBlankQuestionRu.getQuestion())
                .answer(fillBlankQuestionRu.getAnswer())
                .explanation(fillBlankQuestionRu.getExplanation())
                .difficultyLevel(fillBlankQuestionRu.getDifficultyLevel())
                .failCount(fillBlankQuestionRu.getFailCount())
                .successCount(fillBlankQuestionRu.getSuccessCount())
                .tags(fillBlankQuestionRu.getTags())
                .lang(fillBlankQuestionRu.getLang())
                .displayOrder(fillBlankQuestionRu.getDisplayOrder())
                .isActive(fillBlankQuestionRu.getIsActive())
                .createdAt(fillBlankQuestionRu.getCreatedAt())
                .updatedAt(fillBlankQuestionRu.getUpdatedAt())
                .createdBy(fillBlankQuestionRu.getCreatedBy())
                .updatedBy(fillBlankQuestionRu.getUpdatedBy())
                .build();
    }
}
