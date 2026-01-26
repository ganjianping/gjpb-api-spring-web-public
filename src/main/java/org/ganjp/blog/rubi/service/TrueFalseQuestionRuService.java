package org.ganjp.blog.rubi.service;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ganjp.blog.common.exception.ResourceNotFoundException;
import org.ganjp.blog.rubi.model.dto.CreateTrueFalseQuestionRuRequest;
import org.ganjp.blog.rubi.model.dto.TrueFalseQuestionRuResponse;
import org.ganjp.blog.rubi.model.dto.UpdateTrueFalseQuestionRuRequest;
import org.ganjp.blog.rubi.model.entity.TrueFalseQuestionRu;
import org.ganjp.blog.rubi.repository.TrueFalseQuestionRuRepository;
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
public class TrueFalseQuestionRuService {

    private final TrueFalseQuestionRuRepository trueFalseQuestionRuRepository;

    /**
     * Create a new TrueFalseQuestion
     */
    @Transactional
    public TrueFalseQuestionRuResponse createTrueFalseQuestionRu(CreateTrueFalseQuestionRuRequest request, String createdBy) {
        TrueFalseQuestionRu trueFalseQuestionRu = TrueFalseQuestionRu.builder()
                .id(UUID.randomUUID().toString())
                .question(request.getQuestion())
                .answer(request.getAnswer())
                .explanation(request.getExplanation())
                .difficultyLevel(request.getDifficultyLevel())
                .term(request.getTerm())
                .week(request.getWeek())
                .tags(request.getTags())
                .lang(request.getLang())
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .failCount(0)
                .successCount(0)
                .build();

        TrueFalseQuestionRu savedTrueFalseQuestionRu = trueFalseQuestionRuRepository.save(trueFalseQuestionRu);
        log.info("Created TrueFalseQuestion with id: {}", savedTrueFalseQuestionRu.getId());

        return mapToResponse(savedTrueFalseQuestionRu);
    }

    /**
     * Get TrueFalseQuestion by ID
     */
    public TrueFalseQuestionRuResponse getTrueFalseQuestionRuById(String id) {
        TrueFalseQuestionRu trueFalseQuestionRu = trueFalseQuestionRuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TrueFalseQuestion not found with id: " + id));
        return mapToResponse(trueFalseQuestionRu);
    }

    /**
     * Get all TrueFalseQuestions with pagination and filtering
     */
    public Page<TrueFalseQuestionRuResponse> getAllTrueFalseQuestionRus(Pageable pageable, String lang, String difficultyLevel, String tags, Boolean isActive) {
        Specification<TrueFalseQuestionRu> spec = buildSpecification(lang, difficultyLevel, tags, isActive);
        Page<TrueFalseQuestionRu> trueFalseQuestionRus = trueFalseQuestionRuRepository.findAll(spec, pageable);
        return trueFalseQuestionRus.map(this::mapToResponse);
    }

    /**
     * Update TrueFalseQuestion
     */
    @Transactional
    public TrueFalseQuestionRuResponse updateTrueFalseQuestionRu(String id, UpdateTrueFalseQuestionRuRequest request, String updatedBy) {
        TrueFalseQuestionRu trueFalseQuestionRu = trueFalseQuestionRuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TrueFalseQuestion not found with id: " + id));

        if (StringUtils.hasText(request.getQuestion())) {
            trueFalseQuestionRu.setQuestion(request.getQuestion());
        }
        if (request.getAnswer() != null) {
            trueFalseQuestionRu.setAnswer(request.getAnswer());
        }
        if (request.getExplanation() != null) {
            trueFalseQuestionRu.setExplanation(request.getExplanation());
        }
        if (request.getDifficultyLevel() != null) {
            trueFalseQuestionRu.setDifficultyLevel(request.getDifficultyLevel());
        }
        if (request.getTerm() != null) {
            trueFalseQuestionRu.setTerm(request.getTerm());
        }
        if (request.getWeek() != null) {
            trueFalseQuestionRu.setWeek(request.getWeek());
        }
        if (request.getTags() != null) {
            trueFalseQuestionRu.setTags(request.getTags());
        }
        if (request.getLang() != null) {
            trueFalseQuestionRu.setLang(request.getLang());
        }
        if (request.getDisplayOrder() != null) {
            trueFalseQuestionRu.setDisplayOrder(request.getDisplayOrder());
        }
        if (request.getIsActive() != null) {
            trueFalseQuestionRu.setIsActive(request.getIsActive());
        }

        trueFalseQuestionRu.setUpdatedBy(updatedBy);

        TrueFalseQuestionRu updatedTrueFalseQuestionRu = trueFalseQuestionRuRepository.save(trueFalseQuestionRu);
        log.info("Updated TrueFalseQuestion with id: {}", updatedTrueFalseQuestionRu.getId());

        return mapToResponse(updatedTrueFalseQuestionRu);
    }

    /**
     * Delete TrueFalseQuestion (soft delete)
     */
    @Transactional
    public void deleteTrueFalseQuestionRu(String id, String deletedBy) {
        TrueFalseQuestionRu trueFalseQuestionRu = trueFalseQuestionRuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TrueFalseQuestion not found with id: " + id));

        trueFalseQuestionRu.setIsActive(false);
        trueFalseQuestionRu.setUpdatedBy(deletedBy);
        trueFalseQuestionRuRepository.save(trueFalseQuestionRu);

        log.info("Soft deleted TrueFalseQuestion with id: {}", id);
    }

    /**
     * Permanently delete TrueFalseQuestion
     */
    @Transactional
    public void deleteTrueFalseQuestionRuPermanently(String id) {
        TrueFalseQuestionRu trueFalseQuestionRu = trueFalseQuestionRuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TrueFalseQuestion not found with id: " + id));

        trueFalseQuestionRuRepository.delete(trueFalseQuestionRu);
        log.info("Permanently deleted TrueFalseQuestion with id: {}", id);
    }

    /**
     * Get active TrueFalseQuestions by language
     */
    public List<TrueFalseQuestionRuResponse> getActiveTrueFalseQuestionRusByLang(TrueFalseQuestionRu.Language lang) {
        List<TrueFalseQuestionRu> trueFalseQuestionRus = trueFalseQuestionRuRepository.findByLangAndIsActiveTrueOrderByDisplayOrderAsc(lang);
        return trueFalseQuestionRus.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Build specification for filtering
     */
    private Specification<TrueFalseQuestionRu> buildSpecification(String lang, String difficultyLevel, String tags, Boolean isActive) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(lang)) {
                predicates.add(criteriaBuilder.equal(root.get("lang"), TrueFalseQuestionRu.Language.valueOf(lang.toUpperCase())));
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
     * Increment success count for a true false question
     */
    @Transactional
    public void incrementSuccessCount(String id) {
        TrueFalseQuestionRu question = trueFalseQuestionRuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TrueFalseQuestion not found with id: " + id));
        question.setSuccessCount(question.getSuccessCount() + 1);
        trueFalseQuestionRuRepository.save(question);
    }

    /**
     * Increment fail count for a true false question
     */
    @Transactional
    public void incrementFailCount(String id) {
        TrueFalseQuestionRu question = trueFalseQuestionRuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TrueFalseQuestion not found with id: " + id));
        question.setFailCount(question.getFailCount() + 1);
        trueFalseQuestionRuRepository.save(question);
    }

    /**
     * Map entity to response DTO
     */
    private TrueFalseQuestionRuResponse mapToResponse(TrueFalseQuestionRu trueFalseQuestionRu) {
        return TrueFalseQuestionRuResponse.builder()
                .id(trueFalseQuestionRu.getId())
                .question(trueFalseQuestionRu.getQuestion())
                .answer(trueFalseQuestionRu.getAnswer())
                .explanation(trueFalseQuestionRu.getExplanation())
                .difficultyLevel(trueFalseQuestionRu.getDifficultyLevel())
                .failCount(trueFalseQuestionRu.getFailCount())
                .successCount(trueFalseQuestionRu.getSuccessCount())
                .term(trueFalseQuestionRu.getTerm())
                .week(trueFalseQuestionRu.getWeek())
                .tags(trueFalseQuestionRu.getTags())
                .lang(trueFalseQuestionRu.getLang())
                .displayOrder(trueFalseQuestionRu.getDisplayOrder())
                .isActive(trueFalseQuestionRu.getIsActive())
                .createdAt(trueFalseQuestionRu.getCreatedAt())
                .updatedAt(trueFalseQuestionRu.getUpdatedAt())
                .createdBy(trueFalseQuestionRu.getCreatedBy())
                .updatedBy(trueFalseQuestionRu.getUpdatedBy())
                .build();
    }
}
