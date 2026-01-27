package org.ganjp.blog.rubi.service;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ganjp.blog.common.exception.ResourceNotFoundException;
import org.ganjp.blog.rubi.model.dto.CreateFreeTextQuestionRuRequest;
import org.ganjp.blog.rubi.model.dto.FreeTextQuestionRuResponse;
import org.ganjp.blog.rubi.model.dto.UpdateFreeTextQuestionRuRequest;
import org.ganjp.blog.rubi.model.entity.FreeTextQuestionRu;
import org.ganjp.blog.rubi.repository.FreeTextQuestionRuRepository;
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
public class FreeTextQuestionRuService {

    private final FreeTextQuestionRuRepository freeTextQuestionRuRepository;

    /**
     * Create a new FreeTextQuestion
     */
    @Transactional
    public FreeTextQuestionRuResponse createFreeTextQuestionRu(CreateFreeTextQuestionRuRequest request, String createdBy) {
        FreeTextQuestionRu freeTextQuestionRu = FreeTextQuestionRu.builder()
                .id(UUID.randomUUID().toString())
                .question(request.getQuestion())
                .answer(request.getAnswer())
                .description(request.getDescription())
                .questiona(request.getQuestiona())
                .answera(request.getAnswera())
                .questionb(request.getQuestionb())
                .answerb(request.getAnswerb())
                .questionc(request.getQuestionc())
                .answerc(request.getAnswerc())
                .questiond(request.getQuestiond())
                .answerd(request.getAnswerd())
                .questione(request.getQuestione())
                .answere(request.getAnswere())
                .questionf(request.getQuestionf())
                .answerf(request.getAnswerf())
                .explanation(request.getExplanation())
                .difficultyLevel(request.getDifficultyLevel())
                .failCount(0)
                .successCount(0)
                .term(request.getTerm())
                .week(request.getWeek())
                .tags(request.getTags())
                .lang(request.getLang())
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        FreeTextQuestionRu savedFreeTextQuestionRu = freeTextQuestionRuRepository.save(freeTextQuestionRu);
        log.info("Created FreeTextQuestion with id: {}", savedFreeTextQuestionRu.getId());

        return mapToResponse(savedFreeTextQuestionRu);
    }

    /**
     * Get FreeTextQuestion by ID
     */
    public FreeTextQuestionRuResponse getFreeTextQuestionRuById(String id) {
        FreeTextQuestionRu freeTextQuestionRu = freeTextQuestionRuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FreeTextQuestion not found with id: " + id));
        return mapToResponse(freeTextQuestionRu);
    }

    /**
     * Get all FreeTextQuestions with pagination and filtering
     */
    public Page<FreeTextQuestionRuResponse> getAllFreeTextQuestionRus(Pageable pageable, String lang, String difficultyLevel, String tags, Boolean isActive) {
        Specification<FreeTextQuestionRu> spec = buildSpecification(lang, difficultyLevel, tags, isActive);
        Page<FreeTextQuestionRu> freeTextQuestionRus = freeTextQuestionRuRepository.findAll(spec, pageable);
        return freeTextQuestionRus.map(this::mapToResponse);
    }

    /**
     * Update FreeTextQuestion
     */
    @Transactional
    public FreeTextQuestionRuResponse updateFreeTextQuestionRu(String id, UpdateFreeTextQuestionRuRequest request, String updatedBy) {
        FreeTextQuestionRu freeTextQuestionRu = freeTextQuestionRuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FreeTextQuestion not found with id: " + id));

        if (request.getQuestion() != null) {
            freeTextQuestionRu.setQuestion(request.getQuestion());
        }
        if (request.getAnswer() != null) {
            freeTextQuestionRu.setAnswer(request.getAnswer());
        }
        if (request.getDescription() != null) {
            freeTextQuestionRu.setDescription(request.getDescription());
        }
        if (request.getQuestiona() != null) {
            freeTextQuestionRu.setQuestiona(request.getQuestiona());
        }
        if (request.getAnswera() != null) {
            freeTextQuestionRu.setAnswera(request.getAnswera());
        }
        if (request.getQuestionb() != null) {
            freeTextQuestionRu.setQuestionb(request.getQuestionb());
        }
        if (request.getAnswerb() != null) {
            freeTextQuestionRu.setAnswerb(request.getAnswerb());
        }
        if (request.getQuestionc() != null) {
            freeTextQuestionRu.setQuestionc(request.getQuestionc());
        }
        if (request.getAnswerc() != null) {
            freeTextQuestionRu.setAnswerc(request.getAnswerc());
        }
        if (request.getQuestiond() != null) {
            freeTextQuestionRu.setQuestiond(request.getQuestiond());
        }
        if (request.getAnswerd() != null) {
            freeTextQuestionRu.setAnswerd(request.getAnswerd());
        }
        if (request.getQuestione() != null) {
            freeTextQuestionRu.setQuestione(request.getQuestione());
        }
        if (request.getAnswere() != null) {
            freeTextQuestionRu.setAnswere(request.getAnswere());
        }
        if (request.getQuestionf() != null) {
            freeTextQuestionRu.setQuestionf(request.getQuestionf());
        }
        if (request.getAnswerf() != null) {
            freeTextQuestionRu.setAnswerf(request.getAnswerf());
        }
        if (request.getExplanation() != null) {
            freeTextQuestionRu.setExplanation(request.getExplanation());
        }
        if (request.getDifficultyLevel() != null) {
            freeTextQuestionRu.setDifficultyLevel(request.getDifficultyLevel());
        }
        if (request.getTerm() != null) {
            freeTextQuestionRu.setTerm(request.getTerm());
        }
        if (request.getWeek() != null) {
            freeTextQuestionRu.setWeek(request.getWeek());
        }
        if (request.getTags() != null) {
            freeTextQuestionRu.setTags(request.getTags());
        }
        if (request.getLang() != null) {
            freeTextQuestionRu.setLang(request.getLang());
        }
        if (request.getDisplayOrder() != null) {
            freeTextQuestionRu.setDisplayOrder(request.getDisplayOrder());
        }
        if (request.getIsActive() != null) {
            freeTextQuestionRu.setIsActive(request.getIsActive());
        }

        freeTextQuestionRu.setUpdatedBy(updatedBy);

        FreeTextQuestionRu updatedFreeTextQuestionRu = freeTextQuestionRuRepository.save(freeTextQuestionRu);
        log.info("Updated FreeTextQuestion with id: {}", updatedFreeTextQuestionRu.getId());

        return mapToResponse(updatedFreeTextQuestionRu);
    }

    /**
     * Delete FreeTextQuestion (soft delete)
     */
    @Transactional
    public void deleteFreeTextQuestionRu(String id, String deletedBy) {
        FreeTextQuestionRu freeTextQuestionRu = freeTextQuestionRuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FreeTextQuestion not found with id: " + id));

        freeTextQuestionRu.setIsActive(false);
        freeTextQuestionRu.setUpdatedBy(deletedBy);
        freeTextQuestionRuRepository.save(freeTextQuestionRu);

        log.info("Soft deleted FreeTextQuestion with id: {}", id);
    }

    /**
     * Permanently delete FreeTextQuestion
     */
    @Transactional
    public void deleteFreeTextQuestionRuPermanently(String id) {
        FreeTextQuestionRu freeTextQuestionRu = freeTextQuestionRuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FreeTextQuestion not found with id: " + id));

        freeTextQuestionRuRepository.delete(freeTextQuestionRu);
        log.info("Permanently deleted FreeTextQuestion with id: {}", id);
    }

    /**
     * Get active FreeTextQuestions by language
     */
    public List<FreeTextQuestionRuResponse> getActiveFreeTextQuestionRusByLang(FreeTextQuestionRu.Language lang) {
        List<FreeTextQuestionRu> freeTextQuestionRus = freeTextQuestionRuRepository.findByLangAndIsActiveTrueOrderByDisplayOrderAsc(lang);
        return freeTextQuestionRus.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Increment success count for a free text question
     */
    @Transactional
    public void incrementSuccessCount(String id) {
        FreeTextQuestionRu question = freeTextQuestionRuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FreeTextQuestion not found with id: " + id));
        question.setSuccessCount(question.getSuccessCount() + 1);
        freeTextQuestionRuRepository.save(question);
    }

    /**
     * Increment fail count for a free text question
     */
    @Transactional
    public void incrementFailCount(String id) {
        FreeTextQuestionRu question = freeTextQuestionRuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FreeTextQuestion not found with id: " + id));
        question.setFailCount(question.getFailCount() + 1);
        freeTextQuestionRuRepository.save(question);
    }

    /**
     * Build specification for filtering
     */
    private Specification<FreeTextQuestionRu> buildSpecification(String lang, String difficultyLevel, String tags, Boolean isActive) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(lang)) {
                predicates.add(criteriaBuilder.equal(root.get("lang"), FreeTextQuestionRu.Language.valueOf(lang.toUpperCase())));
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
    private FreeTextQuestionRuResponse mapToResponse(FreeTextQuestionRu freeTextQuestionRu) {
        return FreeTextQuestionRuResponse.builder()
                .id(freeTextQuestionRu.getId())
                .question(freeTextQuestionRu.getQuestion())
                .answer(freeTextQuestionRu.getAnswer())
                .description(freeTextQuestionRu.getDescription())
                .questiona(freeTextQuestionRu.getQuestiona())
                .answera(freeTextQuestionRu.getAnswera())
                .questionb(freeTextQuestionRu.getQuestionb())
                .answerb(freeTextQuestionRu.getAnswerb())
                .questionc(freeTextQuestionRu.getQuestionc())
                .answerc(freeTextQuestionRu.getAnswerc())
                .questiond(freeTextQuestionRu.getQuestiond())
                .answerd(freeTextQuestionRu.getAnswerd())
                .questione(freeTextQuestionRu.getQuestione())
                .answere(freeTextQuestionRu.getAnswere())
                .questionf(freeTextQuestionRu.getQuestionf())
                .answerf(freeTextQuestionRu.getAnswerf())
                .explanation(freeTextQuestionRu.getExplanation())
                .difficultyLevel(freeTextQuestionRu.getDifficultyLevel())
                .failCount(freeTextQuestionRu.getFailCount())
                .successCount(freeTextQuestionRu.getSuccessCount())
                .term(freeTextQuestionRu.getTerm())
                .week(freeTextQuestionRu.getWeek())
                .tags(freeTextQuestionRu.getTags())
                .lang(freeTextQuestionRu.getLang())
                .displayOrder(freeTextQuestionRu.getDisplayOrder())
                .isActive(freeTextQuestionRu.getIsActive())
                .createdAt(freeTextQuestionRu.getCreatedAt())
                .updatedAt(freeTextQuestionRu.getUpdatedAt())
                .createdBy(freeTextQuestionRu.getCreatedBy())
                .updatedBy(freeTextQuestionRu.getUpdatedBy())
                .build();
    }
}