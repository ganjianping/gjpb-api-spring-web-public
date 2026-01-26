package org.ganjp.blog.rubi.service;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ganjp.blog.common.exception.ResourceNotFoundException;
import org.ganjp.blog.rubi.model.dto.CreateMultipleChoiceQuestionRuRequest;
import org.ganjp.blog.rubi.model.dto.MultipleChoiceQuestionRuResponse;
import org.ganjp.blog.rubi.model.dto.UpdateMultipleChoiceQuestionRuRequest;
import org.ganjp.blog.rubi.model.entity.MultipleChoiceQuestionRu;
import org.ganjp.blog.rubi.repository.MultipleChoiceQuestionRuRepository;
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
public class MultipleChoiceQuestionRuService {

    private final MultipleChoiceQuestionRuRepository multipleChoiceQuestionRuRepository;

    /**
     * Create a new MultipleChoiceQuestion
     */
    @Transactional
    public MultipleChoiceQuestionRuResponse createMultipleChoiceQuestionRu(CreateMultipleChoiceQuestionRuRequest request, String createdBy) {
        MultipleChoiceQuestionRu multipleChoiceQuestionRu = MultipleChoiceQuestionRu.builder()
                .id(UUID.randomUUID().toString())
                .question(request.getQuestion())
                .optionA(request.getOptionA())
                .optionB(request.getOptionB())
                .optionC(request.getOptionC())
                .optionD(request.getOptionD())
                .answer(request.getAnswer())
                .explanation(request.getExplanation())
                .difficultyLevel(request.getDifficultyLevel())
                .term(request.getTerm())
                .week(request.getWeek())
                .tags(request.getTags())
                .lang(request.getLang())
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        MultipleChoiceQuestionRu savedMultipleChoiceQuestionRu = multipleChoiceQuestionRuRepository.save(multipleChoiceQuestionRu);
        log.info("Created MultipleChoiceQuestion with id: {}", savedMultipleChoiceQuestionRu.getId());

        return mapToResponse(savedMultipleChoiceQuestionRu);
    }

    /**
     * Get MultipleChoiceQuestion by ID
     */
    public MultipleChoiceQuestionRuResponse getMultipleChoiceQuestionRuById(String id) {
        MultipleChoiceQuestionRu multipleChoiceQuestionRu = multipleChoiceQuestionRuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MultipleChoiceQuestion not found with id: " + id));
        return mapToResponse(multipleChoiceQuestionRu);
    }

    /**
     * Get all MultipleChoiceQuestions with pagination and filtering
     */
    public Page<MultipleChoiceQuestionRuResponse> getAllMultipleChoiceQuestionRus(Pageable pageable, String lang, String difficultyLevel, String tags, Boolean isActive) {
        Specification<MultipleChoiceQuestionRu> spec = buildSpecification(lang, difficultyLevel, tags, isActive);
        Page<MultipleChoiceQuestionRu> multipleChoiceQuestionRus = multipleChoiceQuestionRuRepository.findAll(spec, pageable);
        return multipleChoiceQuestionRus.map(this::mapToResponse);
    }

    /**
     * Update MultipleChoiceQuestion
     */
    @Transactional
    public MultipleChoiceQuestionRuResponse updateMultipleChoiceQuestionRu(String id, UpdateMultipleChoiceQuestionRuRequest request, String updatedBy) {
        MultipleChoiceQuestionRu multipleChoiceQuestionRu = multipleChoiceQuestionRuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MultipleChoiceQuestion not found with id: " + id));

        if (StringUtils.hasText(request.getQuestion())) {
            multipleChoiceQuestionRu.setQuestion(request.getQuestion());
        }
        if (StringUtils.hasText(request.getOptionA())) {
            multipleChoiceQuestionRu.setOptionA(request.getOptionA());
        }
        if (StringUtils.hasText(request.getOptionB())) {
            multipleChoiceQuestionRu.setOptionB(request.getOptionB());
        }
        if (StringUtils.hasText(request.getOptionC())) {
            multipleChoiceQuestionRu.setOptionC(request.getOptionC());
        }
        if (StringUtils.hasText(request.getOptionD())) {
            multipleChoiceQuestionRu.setOptionD(request.getOptionD());
        }
        if (StringUtils.hasText(request.getAnswer())) {
            multipleChoiceQuestionRu.setAnswer(request.getAnswer());
        }
        if (request.getExplanation() != null) {
            multipleChoiceQuestionRu.setExplanation(request.getExplanation());
        }
        if (request.getDifficultyLevel() != null) {
            multipleChoiceQuestionRu.setDifficultyLevel(request.getDifficultyLevel());
        }
        if (request.getTerm() != null) {
            multipleChoiceQuestionRu.setTerm(request.getTerm());
        }
        if (request.getWeek() != null) {
            multipleChoiceQuestionRu.setWeek(request.getWeek());
        }
        if (request.getTags() != null) {
            multipleChoiceQuestionRu.setTags(request.getTags());
        }
        if (request.getLang() != null) {
            multipleChoiceQuestionRu.setLang(request.getLang());
        }
        if (request.getDisplayOrder() != null) {
            multipleChoiceQuestionRu.setDisplayOrder(request.getDisplayOrder());
        }
        if (request.getIsActive() != null) {
            multipleChoiceQuestionRu.setIsActive(request.getIsActive());
        }

        multipleChoiceQuestionRu.setUpdatedBy(updatedBy);

        MultipleChoiceQuestionRu updatedMultipleChoiceQuestionRu = multipleChoiceQuestionRuRepository.save(multipleChoiceQuestionRu);
        log.info("Updated MultipleChoiceQuestion with id: {}", updatedMultipleChoiceQuestionRu.getId());

        return mapToResponse(updatedMultipleChoiceQuestionRu);
    }

    /**
     * Delete MultipleChoiceQuestion (soft delete)
     */
    @Transactional
    public void deleteMultipleChoiceQuestionRu(String id, String deletedBy) {
        MultipleChoiceQuestionRu multipleChoiceQuestionRu = multipleChoiceQuestionRuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MultipleChoiceQuestion not found with id: " + id));

        multipleChoiceQuestionRu.setIsActive(false);
        multipleChoiceQuestionRu.setUpdatedBy(deletedBy);
        multipleChoiceQuestionRuRepository.save(multipleChoiceQuestionRu);

        log.info("Soft deleted MultipleChoiceQuestion with id: {}", id);
    }

    /**
     * Permanently delete MultipleChoiceQuestion
     */
    @Transactional
    public void deleteMultipleChoiceQuestionRuPermanently(String id) {
        MultipleChoiceQuestionRu multipleChoiceQuestionRu = multipleChoiceQuestionRuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MultipleChoiceQuestion not found with id: " + id));

        multipleChoiceQuestionRuRepository.delete(multipleChoiceQuestionRu);
        log.info("Permanently deleted MultipleChoiceQuestion with id: {}", id);
    }

    /**
     * Get active MultipleChoiceQuestions by language
     */
    public List<MultipleChoiceQuestionRuResponse> getActiveMultipleChoiceQuestionRusByLang(MultipleChoiceQuestionRu.Language lang) {
        List<MultipleChoiceQuestionRu> multipleChoiceQuestionRus = multipleChoiceQuestionRuRepository.findByLangAndIsActiveTrueOrderByDisplayOrderAsc(lang);
        return multipleChoiceQuestionRus.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Build specification for filtering
     */
    private Specification<MultipleChoiceQuestionRu> buildSpecification(String lang, String difficultyLevel, String tags, Boolean isActive) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(lang)) {
                predicates.add(criteriaBuilder.equal(root.get("lang"), MultipleChoiceQuestionRu.Language.valueOf(lang.toUpperCase())));
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
     * Increment success count for a multiple choice question
     */
    @Transactional
    public void incrementSuccessCount(String id) {
        MultipleChoiceQuestionRu question = multipleChoiceQuestionRuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MultipleChoiceQuestion not found with id: " + id));
        question.setSuccessCount(question.getSuccessCount() + 1);
        multipleChoiceQuestionRuRepository.save(question);
    }

    /**
     * Increment fail count for a multiple choice question
     */
    @Transactional
    public void incrementFailCount(String id) {
        MultipleChoiceQuestionRu question = multipleChoiceQuestionRuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MultipleChoiceQuestion not found with id: " + id));
        question.setFailCount(question.getFailCount() + 1);
        multipleChoiceQuestionRuRepository.save(question);
    }

    /**
     * Map entity to response DTO
     */
    private MultipleChoiceQuestionRuResponse mapToResponse(MultipleChoiceQuestionRu multipleChoiceQuestionRu) {
        return MultipleChoiceQuestionRuResponse.builder()
                .id(multipleChoiceQuestionRu.getId())
                .question(multipleChoiceQuestionRu.getQuestion())
                .optionA(multipleChoiceQuestionRu.getOptionA())
                .optionB(multipleChoiceQuestionRu.getOptionB())
                .optionC(multipleChoiceQuestionRu.getOptionC())
                .optionD(multipleChoiceQuestionRu.getOptionD())
                .answer(multipleChoiceQuestionRu.getAnswer())
                .explanation(multipleChoiceQuestionRu.getExplanation())
                .difficultyLevel(multipleChoiceQuestionRu.getDifficultyLevel())
                .failCount(multipleChoiceQuestionRu.getFailCount())
                .successCount(multipleChoiceQuestionRu.getSuccessCount())
                .term(multipleChoiceQuestionRu.getTerm())
                .week(multipleChoiceQuestionRu.getWeek())
                .tags(multipleChoiceQuestionRu.getTags())
                .lang(multipleChoiceQuestionRu.getLang())
                .displayOrder(multipleChoiceQuestionRu.getDisplayOrder())
                .isActive(multipleChoiceQuestionRu.getIsActive())
                .createdAt(multipleChoiceQuestionRu.getCreatedAt())
                .updatedAt(multipleChoiceQuestionRu.getUpdatedAt())
                .createdBy(multipleChoiceQuestionRu.getCreatedBy())
                .updatedBy(multipleChoiceQuestionRu.getUpdatedBy())
                .build();
    }
}