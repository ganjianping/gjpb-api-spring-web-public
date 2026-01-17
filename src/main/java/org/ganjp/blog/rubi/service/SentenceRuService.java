package org.ganjp.blog.rubi.service;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ganjp.blog.common.exception.BusinessException;
import org.ganjp.blog.common.exception.ResourceNotFoundException;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SentenceRuService {

    private final SentenceRuRepository sentenceRepository;

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

        SentenceRu savedSentence = sentenceRepository.save(dbSentence);
        return SentenceRuResponse.fromEntity(savedSentence);
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

        dbSentence.setUpdatedBy(updatedBy);

        SentenceRu updatedSentence = sentenceRepository.save(dbSentence);
        return SentenceRuResponse.fromEntity(updatedSentence);
    }

    /**
     * Get sentence by ID
     */
    public SentenceRuResponse getSentenceById(String id) {
        SentenceRu sentence = sentenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sentence not found with id: " + id));
        return SentenceRuResponse.fromEntity(sentence);
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
    public Page<SentenceRuResponse> getSentences(String name, SentenceRu.Language lang, String tags, Boolean isActive, Pageable pageable) {
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

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return sentenceRepository.findAll(spec, pageable).map(SentenceRuResponse::fromEntity);
    }
    
    /**
     * Get active sentences by language
     */
    public List<SentenceRuResponse> getSentencesByLanguage(SentenceRu.Language lang) {
        return sentenceRepository.findByLangAndIsActiveTrueOrderByDisplayOrderAsc(lang)
                .stream()
                .map(SentenceRuResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
