package org.ganjp.blog.rubi.service;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ganjp.blog.common.exception.ResourceNotFoundException;
import org.ganjp.blog.rubi.model.dto.CreateMcqRuRequest;
import org.ganjp.blog.rubi.model.dto.McqRuResponse;
import org.ganjp.blog.rubi.model.dto.UpdateMcqRuRequest;
import org.ganjp.blog.rubi.model.entity.McqRu;
import org.ganjp.blog.rubi.repository.McqRuRepository;
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
public class McqRuService {

    private final McqRuRepository mcqRuRepository;

    /**
     * Create a new MCQ
     */
    @Transactional
    public McqRuResponse createMcq(CreateMcqRuRequest request, String createdBy) {
        McqRu mcqRu = McqRu.builder()
                .id(UUID.randomUUID().toString())
                .question(request.getQuestion())
                .optionA(request.getOptionA())
                .optionB(request.getOptionB())
                .optionC(request.getOptionC())
                .optionD(request.getOptionD())
                .correctAnswers(request.getCorrectAnswers())
                .isMultipleCorrect(request.getIsMultipleCorrect() != null ? request.getIsMultipleCorrect() : false)
                .explanation(request.getExplanation())
                .difficultyLevel(request.getDifficultyLevel())
                .tags(request.getTags())
                .lang(request.getLang())
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        McqRu savedMcq = mcqRuRepository.save(mcqRu);
        log.info("Created MCQ with id: {}", savedMcq.getId());

        return mapToResponse(savedMcq);
    }

    /**
     * Get MCQ by ID
     */
    public McqRuResponse getMcqById(String id) {
        McqRu mcqRu = mcqRuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MCQ not found with id: " + id));
        return mapToResponse(mcqRu);
    }

    /**
     * Get all MCQs with pagination and filtering
     */
    public Page<McqRuResponse> getAllMcqs(Pageable pageable, String lang, String difficultyLevel, String tags, Boolean isActive) {
        Specification<McqRu> spec = buildSpecification(lang, difficultyLevel, tags, isActive);
        Page<McqRu> mcqs = mcqRuRepository.findAll(spec, pageable);
        return mcqs.map(this::mapToResponse);
    }

    /**
     * Update MCQ
     */
    @Transactional
    public McqRuResponse updateMcq(String id, UpdateMcqRuRequest request, String updatedBy) {
        McqRu mcqRu = mcqRuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MCQ not found with id: " + id));

        if (StringUtils.hasText(request.getQuestion())) {
            mcqRu.setQuestion(request.getQuestion());
        }
        if (StringUtils.hasText(request.getOptionA())) {
            mcqRu.setOptionA(request.getOptionA());
        }
        if (StringUtils.hasText(request.getOptionB())) {
            mcqRu.setOptionB(request.getOptionB());
        }
        if (StringUtils.hasText(request.getOptionC())) {
            mcqRu.setOptionC(request.getOptionC());
        }
        if (StringUtils.hasText(request.getOptionD())) {
            mcqRu.setOptionD(request.getOptionD());
        }
        if (StringUtils.hasText(request.getCorrectAnswers())) {
            mcqRu.setCorrectAnswers(request.getCorrectAnswers());
        }
        if (request.getIsMultipleCorrect() != null) {
            mcqRu.setIsMultipleCorrect(request.getIsMultipleCorrect());
        }
        if (request.getExplanation() != null) {
            mcqRu.setExplanation(request.getExplanation());
        }
        if (request.getDifficultyLevel() != null) {
            mcqRu.setDifficultyLevel(request.getDifficultyLevel());
        }
        if (request.getTags() != null) {
            mcqRu.setTags(request.getTags());
        }
        if (request.getLang() != null) {
            mcqRu.setLang(request.getLang());
        }
        if (request.getDisplayOrder() != null) {
            mcqRu.setDisplayOrder(request.getDisplayOrder());
        }
        if (request.getIsActive() != null) {
            mcqRu.setIsActive(request.getIsActive());
        }

        mcqRu.setUpdatedBy(updatedBy);

        McqRu updatedMcq = mcqRuRepository.save(mcqRu);
        log.info("Updated MCQ with id: {}", updatedMcq.getId());

        return mapToResponse(updatedMcq);
    }

    /**
     * Delete MCQ (soft delete)
     */
    @Transactional
    public void deleteMcq(String id, String deletedBy) {
        McqRu mcqRu = mcqRuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MCQ not found with id: " + id));

        mcqRu.setIsActive(false);
        mcqRu.setUpdatedBy(deletedBy);
        mcqRuRepository.save(mcqRu);

        log.info("Soft deleted MCQ with id: {}", id);
    }

    /**
     * Get active MCQs by language
     */
    public List<McqRuResponse> getActiveMcqsByLang(McqRu.Language lang) {
        List<McqRu> mcqs = mcqRuRepository.findByLangAndIsActiveTrueOrderByDisplayOrderAsc(lang);
        return mcqs.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Build specification for filtering
     */
    private Specification<McqRu> buildSpecification(String lang, String difficultyLevel, String tags, Boolean isActive) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(lang)) {
                predicates.add(criteriaBuilder.equal(root.get("lang"), McqRu.Language.valueOf(lang.toUpperCase())));
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
    private McqRuResponse mapToResponse(McqRu mcqRu) {
        return McqRuResponse.builder()
                .id(mcqRu.getId())
                .question(mcqRu.getQuestion())
                .optionA(mcqRu.getOptionA())
                .optionB(mcqRu.getOptionB())
                .optionC(mcqRu.getOptionC())
                .optionD(mcqRu.getOptionD())
                .correctAnswers(mcqRu.getCorrectAnswers())
                .isMultipleCorrect(mcqRu.getIsMultipleCorrect())
                .explanation(mcqRu.getExplanation())
                .difficultyLevel(mcqRu.getDifficultyLevel())
                .failCount(mcqRu.getFailCount())
                .successCount(mcqRu.getSuccessCount())
                .tags(mcqRu.getTags())
                .lang(mcqRu.getLang())
                .displayOrder(mcqRu.getDisplayOrder())
                .isActive(mcqRu.getIsActive())
                .createdAt(mcqRu.getCreatedAt())
                .updatedAt(mcqRu.getUpdatedAt())
                .createdBy(mcqRu.getCreatedBy())
                .updatedBy(mcqRu.getUpdatedBy())
                .build();
    }
}