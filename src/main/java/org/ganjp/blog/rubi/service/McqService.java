package org.ganjp.blog.rubi.service;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ganjp.blog.common.exception.ResourceNotFoundException;
import org.ganjp.blog.rubi.model.dto.CreateMcqRequest;
import org.ganjp.blog.rubi.model.dto.McqResponse;
import org.ganjp.blog.rubi.model.dto.UpdateMcqRequest;
import org.ganjp.blog.rubi.model.entity.Mcq;
import org.ganjp.blog.rubi.repository.McqRepository;
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
public class McqService {

    private final McqRepository mcqRepository;

    /**
     * Create a new MCQ
     */
    @Transactional
    public McqResponse createMcq(CreateMcqRequest request, String createdBy) {
        Mcq mcq = Mcq.builder()
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

        Mcq savedMcq = mcqRepository.save(mcq);
        log.info("Created MCQ with id: {}", savedMcq.getId());

        return mapToResponse(savedMcq);
    }

    /**
     * Get MCQ by ID
     */
    public McqResponse getMcqById(String id) {
        Mcq mcq = mcqRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MCQ not found with id: " + id));
        return mapToResponse(mcq);
    }

    /**
     * Get all MCQs with pagination and filtering
     */
    public Page<McqResponse> getAllMcqs(Pageable pageable, String lang, String difficultyLevel, String tags, Boolean isActive) {
        Specification<Mcq> spec = buildSpecification(lang, difficultyLevel, tags, isActive);
        Page<Mcq> mcqs = mcqRepository.findAll(spec, pageable);
        return mcqs.map(this::mapToResponse);
    }

    /**
     * Update MCQ
     */
    @Transactional
    public McqResponse updateMcq(String id, UpdateMcqRequest request, String updatedBy) {
        Mcq mcq = mcqRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MCQ not found with id: " + id));

        if (StringUtils.hasText(request.getQuestion())) {
            mcq.setQuestion(request.getQuestion());
        }
        if (StringUtils.hasText(request.getOptionA())) {
            mcq.setOptionA(request.getOptionA());
        }
        if (StringUtils.hasText(request.getOptionB())) {
            mcq.setOptionB(request.getOptionB());
        }
        if (StringUtils.hasText(request.getOptionC())) {
            mcq.setOptionC(request.getOptionC());
        }
        if (StringUtils.hasText(request.getOptionD())) {
            mcq.setOptionD(request.getOptionD());
        }
        if (StringUtils.hasText(request.getCorrectAnswers())) {
            mcq.setCorrectAnswers(request.getCorrectAnswers());
        }
        if (request.getIsMultipleCorrect() != null) {
            mcq.setIsMultipleCorrect(request.getIsMultipleCorrect());
        }
        if (request.getExplanation() != null) {
            mcq.setExplanation(request.getExplanation());
        }
        if (request.getDifficultyLevel() != null) {
            mcq.setDifficultyLevel(request.getDifficultyLevel());
        }
        if (request.getTags() != null) {
            mcq.setTags(request.getTags());
        }
        if (request.getLang() != null) {
            mcq.setLang(request.getLang());
        }
        if (request.getDisplayOrder() != null) {
            mcq.setDisplayOrder(request.getDisplayOrder());
        }
        if (request.getIsActive() != null) {
            mcq.setIsActive(request.getIsActive());
        }

        mcq.setUpdatedBy(updatedBy);

        Mcq updatedMcq = mcqRepository.save(mcq);
        log.info("Updated MCQ with id: {}", updatedMcq.getId());

        return mapToResponse(updatedMcq);
    }

    /**
     * Delete MCQ (soft delete)
     */
    @Transactional
    public void deleteMcq(String id, String deletedBy) {
        Mcq mcq = mcqRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MCQ not found with id: " + id));

        mcq.setIsActive(false);
        mcq.setUpdatedBy(deletedBy);
        mcqRepository.save(mcq);

        log.info("Soft deleted MCQ with id: {}", id);
    }

    /**
     * Get active MCQs by language
     */
    public List<McqResponse> getActiveMcqsByLang(Mcq.Language lang) {
        List<Mcq> mcqs = mcqRepository.findByLangAndIsActiveTrueOrderByDisplayOrderAsc(lang);
        return mcqs.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Build specification for filtering
     */
    private Specification<Mcq> buildSpecification(String lang, String difficultyLevel, String tags, Boolean isActive) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(lang)) {
                predicates.add(criteriaBuilder.equal(root.get("lang"), Mcq.Language.valueOf(lang.toUpperCase())));
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
    private McqResponse mapToResponse(Mcq mcq) {
        return McqResponse.builder()
                .id(mcq.getId())
                .question(mcq.getQuestion())
                .optionA(mcq.getOptionA())
                .optionB(mcq.getOptionB())
                .optionC(mcq.getOptionC())
                .optionD(mcq.getOptionD())
                .correctAnswers(mcq.getCorrectAnswers())
                .isMultipleCorrect(mcq.getIsMultipleCorrect())
                .explanation(mcq.getExplanation())
                .difficultyLevel(mcq.getDifficultyLevel())
                .failCount(mcq.getFailCount())
                .successCount(mcq.getSuccessCount())
                .tags(mcq.getTags())
                .lang(mcq.getLang())
                .displayOrder(mcq.getDisplayOrder())
                .isActive(mcq.getIsActive())
                .createdAt(mcq.getCreatedAt())
                .updatedAt(mcq.getUpdatedAt())
                .createdBy(mcq.getCreatedBy())
                .updatedBy(mcq.getUpdatedBy())
                .build();
    }
}