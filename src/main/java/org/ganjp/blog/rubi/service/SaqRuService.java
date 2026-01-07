package org.ganjp.blog.rubi.service;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ganjp.blog.common.exception.ResourceNotFoundException;
import org.ganjp.blog.rubi.model.dto.CreateSaqRuRequest;
import org.ganjp.blog.rubi.model.dto.SaqRuResponse;
import org.ganjp.blog.rubi.model.dto.UpdateSaqRuRequest;
import org.ganjp.blog.rubi.model.entity.SaqRu;
import org.ganjp.blog.rubi.repository.SaqRuRepository;
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
public class SaqRuService {

    private final SaqRuRepository saqRuRepository;

    /**
     * Create a new SAQ
     */
    @Transactional
    public SaqRuResponse createSaq(CreateSaqRuRequest request, String createdBy) {
        SaqRu saq = SaqRu.builder()
                .id(UUID.randomUUID().toString())
                .question(request.getQuestion())
                .correctAnswer(request.getCorrectAnswer())
                .explanation(request.getExplanation())
                .difficultyLevel(request.getDifficultyLevel())
                .tags(request.getTags())
                .lang(request.getLang())
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        SaqRu savedSaq = saqRuRepository.save(saq);
        log.info("Created SAQ with id: {}", savedSaq.getId());

        return mapToResponse(savedSaq);
    }

    /**
     * Get SAQ by ID
     */
    public SaqRuResponse getSaqById(String id) {
        SaqRu saq = saqRuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SAQ not found with id: " + id));
        return mapToResponse(saq);
    }

    /**
     * Get all SAQs with pagination and filtering
     */
    public Page<SaqRuResponse> getAllSaqs(Pageable pageable, String lang, String difficultyLevel, String tags, Boolean isActive) {
        Specification<SaqRu> spec = buildSpecification(lang, difficultyLevel, tags, isActive);
        Page<SaqRu> saqs = saqRuRepository.findAll(spec, pageable);
        return saqs.map(this::mapToResponse);
    }

    /**
     * Update SAQ
     */
    @Transactional
    public SaqRuResponse updateSaq(String id, UpdateSaqRuRequest request, String updatedBy) {
        SaqRu saq = saqRuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SAQ not found with id: " + id));

        if (StringUtils.hasText(request.getQuestion())) {
            saq.setQuestion(request.getQuestion());
        }
        if (StringUtils.hasText(request.getCorrectAnswer())) {
            saq.setCorrectAnswer(request.getCorrectAnswer());
        }
        if (request.getExplanation() != null) {
            saq.setExplanation(request.getExplanation());
        }
        if (request.getDifficultyLevel() != null) {
            saq.setDifficultyLevel(request.getDifficultyLevel());
        }
        if (request.getTags() != null) {
            saq.setTags(request.getTags());
        }
        if (request.getLang() != null) {
            saq.setLang(request.getLang());
        }
        if (request.getDisplayOrder() != null) {
            saq.setDisplayOrder(request.getDisplayOrder());
        }
        if (request.getIsActive() != null) {
            saq.setIsActive(request.getIsActive());
        }

        saq.setUpdatedBy(updatedBy);

        SaqRu updatedSaq = saqRuRepository.save(saq);
        log.info("Updated SAQ with id: {}", updatedSaq.getId());

        return mapToResponse(updatedSaq);
    }

    /**
     * Delete SAQ (soft delete)
     */
    @Transactional
    public void deleteSaq(String id, String deletedBy) {
        SaqRu saq = saqRuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SAQ not found with id: " + id));

        saq.setIsActive(false);
        saq.setUpdatedBy(deletedBy);
        saqRuRepository.save(saq);

        log.info("Soft deleted SAQ with id: {}", id);
    }

    /**
     * Get active SAQs by language
     */
    public List<SaqRuResponse> getActiveSaqsByLang(SaqRu.Language lang) {
        List<SaqRu> saqs = saqRuRepository.findByLangAndIsActiveTrueOrderByDisplayOrderAsc(lang);
        return saqs.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Build specification for filtering
     */
    private Specification<SaqRu> buildSpecification(String lang, String difficultyLevel, String tags, Boolean isActive) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(lang)) {
                predicates.add(criteriaBuilder.equal(root.get("lang"), SaqRu.Language.valueOf(lang.toUpperCase())));
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
    private SaqRuResponse mapToResponse(SaqRu saq) {
        return SaqRuResponse.builder()
                .id(saq.getId())
                .question(saq.getQuestion())
                .correctAnswer(saq.getCorrectAnswer())
                .explanation(saq.getExplanation())
                .difficultyLevel(saq.getDifficultyLevel())
                .tags(saq.getTags())
                .lang(saq.getLang())
                .displayOrder(saq.getDisplayOrder())
                .isActive(saq.getIsActive())
                .createdAt(saq.getCreatedAt())
                .updatedAt(saq.getUpdatedAt())
                .createdBy(saq.getCreatedBy())
                .updatedBy(saq.getUpdatedBy())
                .build();
    }
}