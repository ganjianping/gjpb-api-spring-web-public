package org.ganjp.blog.rubi.service;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ganjp.blog.common.exception.BusinessException;
import org.ganjp.blog.common.exception.ResourceNotFoundException;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExpressionRuService {

    private final ExpressionRuRepository expressionRepository;

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

        ExpressionRu savedExpression = expressionRepository.save(dbExpression);
        return ExpressionRuResponse.fromEntity(savedExpression);
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

        dbExpression.setUpdatedBy(updatedBy);

        ExpressionRu updatedExpression = expressionRepository.save(dbExpression);
        return ExpressionRuResponse.fromEntity(updatedExpression);
    }

    /**
     * Get expression by ID
     */
    public ExpressionRuResponse getExpressionById(String id) {
        ExpressionRu expression = expressionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expression not found with id: " + id));
        return ExpressionRuResponse.fromEntity(expression);
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
    public Page<ExpressionRuResponse> getExpressions(String name, ExpressionRu.Language lang, String tags, Boolean isActive, Pageable pageable) {
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

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return expressionRepository.findAll(spec, pageable).map(ExpressionRuResponse::fromEntity);
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
}
