package org.ganjp.blog.open.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ganjp.blog.common.model.PaginatedResponse;
import org.ganjp.blog.open.model.PublicVocabularyRuResponse;
import org.ganjp.blog.rubi.model.dto.VocabularyRuResponse;
import org.ganjp.blog.rubi.model.entity.VocabularyRu;
import org.ganjp.blog.rubi.service.VocabularyRuService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PublicRubiService {
    private final VocabularyRuService vocabularyRuService;
    
    @Value("${rubi.vocabulary.base-url:}")
    private String vocabularyBaseUrl;

    public PaginatedResponse<PublicVocabularyRuResponse> getVocabularies(String word, VocabularyRu.Language lang, String tags, Integer term, Integer week, String difficultyLevel, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "displayOrder"));
        Page<VocabularyRuResponse> pageResult = vocabularyRuService.getVocabularies(word, lang, tags, true, term, week, difficultyLevel, pageable);

        // Map internal VocabularyRuResponse -> PublicVocabularyRuResponse and compute urls
        List<PublicVocabularyRuResponse> publicList = pageResult.getContent().stream().map(r -> {
            PublicVocabularyRuResponse.PublicVocabularyRuResponseBuilder b = PublicVocabularyRuResponse.builder()
                .id(r.getId())
                .name(r.getName())
                .phonetic(r.getPhonetic())
                .partOfSpeech(r.getPartOfSpeech())
                .nounPluralForm(r.getNounPluralForm())
                .nounForm(r.getNounForm())
                .nounMeaning(r.getNounMeaning())
                .nounExample(r.getNounExample())
                .verbSimplePastTense(r.getVerbSimplePastTense())
                .verbPastPerfectTense(r.getVerbPastPerfectTense())
                .verbPresentParticiple(r.getVerbPresentParticiple())
                .adjectiveComparativeForm(r.getAdjectiveComparativeForm())
                .adjectiveSuperlativeForm(r.getAdjectiveSuperlativeForm())
                .verbForm(r.getVerbForm())
                .verbMeaning(r.getVerbMeaning())
                .verbExample(r.getVerbExample())
                .adjectiveForm(r.getAdjectiveForm())
                .adjectiveMeaning(r.getAdjectiveMeaning())
                .adjectiveExample(r.getAdjectiveExample())
                .adverbForm(r.getAdverbForm())
                .adverbMeaning(r.getAdverbMeaning())
                .adverbExample(r.getAdverbExample())
                .translation(r.getTranslation())
                .synonyms(r.getSynonyms())
                .definition(r.getDefinition())
                .example(r.getExample())
                .dictionaryUrl(r.getDictionaryUrl())
                .term(r.getTerm())
                .week(r.getWeek())
                .tags(r.getTags())
                .difficultyLevel(r.getDifficultyLevel())
                .lang(r.getLang())
                .displayOrder(r.getDisplayOrder())
                .updatedAt(r.getUpdatedAt() != null ? r.getUpdatedAt().toString() : null);

            // Build imageUrl from imageFilename
            String imageFilename = r.getImageFilename();
            b.imageUrl(joinBasePathWithSegment(vocabularyBaseUrl, "images", imageFilename));

            // Build phoneticAudioUrl from phoneticAudioFilename
            String audioFilename = r.getPhoneticAudioFilename();
            b.phoneticAudioUrl(joinBasePathWithSegment(vocabularyBaseUrl, "audios", audioFilename));

            return b.build();
        }).toList();

        return PaginatedResponse.of(publicList, page, size, pageResult.getTotalElements());
    }

     /**
     * Join base + segment + path. Segment should not be null (e.g. "images").
     */
    private String joinBasePathWithSegment(String base, String segment, String path) {
        if (path == null || path.isBlank()) return null;
        if (segment == null) segment = "";
        // normalize
        String seg = segment;
        if (!seg.endsWith("/")) seg = seg + "/";
        if (base != null && !base.isBlank()) {
            String prefix = base;
            if (!prefix.endsWith("/")) prefix = prefix + "/";
            // avoid double slashes between prefix and seg
            if (prefix.endsWith("/") && seg.startsWith("/")) seg = seg.substring(1);
            String p = path.startsWith("/") ? path.substring(1) : path;
            return prefix + seg + p;
        }
        // no base; if path is absolute return as-is, else return seg+path with leading '/'
        if (path.startsWith("http") || path.startsWith("/")) return path;
        return "/" + seg + (path.startsWith("/") ? path.substring(1) : path);
    }
}
