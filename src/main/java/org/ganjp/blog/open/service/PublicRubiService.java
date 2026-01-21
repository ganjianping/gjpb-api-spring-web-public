package org.ganjp.blog.open.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ganjp.blog.common.model.PaginatedResponse;
import org.ganjp.blog.open.model.*;
import org.ganjp.blog.rubi.model.dto.*;
import org.ganjp.blog.rubi.model.entity.*;
import org.ganjp.blog.rubi.service.*;
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
    private final ExpressionRuService expressionRuService;
    private final SentenceRuService sentenceRuService;
    private final MultipleChoiceQuestionRuService multipleChoiceQuestionRuService;
    private final TrueFalseQuestionRuService trueFalseQuestionRuService;
    private final FreeTextQuestionRuService freeTextQuestionRuService;
    private final FillBlankQuestionRuService fillBlankQuestionRuService;
    private final ArticleRuService articleRuService;
    private final AudioRuService audioRuService;
    private final ImageRuService imageRuService;
    private final VideoRuService videoRuService;
    
    @Value("${rubi.vocabulary.base-url:}")
    private String vocabularyBaseUrl;
    
    @Value("${rubi.article.cover-image.base-url:}")
    private String articleCoverImageBaseUrl;
    
    @Value("${rubi.audio.base-url:}")
    private String audioBaseUrl;
    
    @Value("${rubi.image.base-url:}")
    private String imageBaseUrl;
    
    @Value("${rubi.video.base-url:}")
    private String videoBaseUrl;

    public PaginatedResponse<PublicVocabularyRuResponse> getVocabularies(String word, VocabularyRu.Language lang, String tags, Integer term, Integer week, String difficultyLevel, String partOfSpeech, int page, int size, String sort, String direction) {
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        Page<VocabularyRuResponse> pageResult = vocabularyRuService.getVocabularies(word, lang, tags, true, term, week, difficultyLevel, partOfSpeech, pageable);

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

    public PaginatedResponse<PublicExpressionRuResponse> getExpressions(String name, ExpressionRu.Language lang, String tags, Integer term, Integer week, String difficultyLevel, int page, int size, String sort, String direction) {
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        Page<ExpressionRuResponse> pageResult = expressionRuService.getExpressions(name, lang, tags, true, term, week, difficultyLevel, pageable);

        List<PublicExpressionRuResponse> publicList = pageResult.getContent().stream().map(r ->
            PublicExpressionRuResponse.builder()
                .id(r.getId())
                .name(r.getName())
                .phonetic(r.getPhonetic())
                .translation(r.getTranslation())
                .explanation(r.getExplanation())
                .example(r.getExample())
                .term(r.getTerm())
                .week(r.getWeek())
                .tags(r.getTags())
                .difficultyLevel(r.getDifficultyLevel())
                .lang(r.getLang())
                .displayOrder(r.getDisplayOrder())
                .updatedAt(r.getUpdatedAt() != null ? r.getUpdatedAt().toString() : null)
                .build()
        ).toList();

        return PaginatedResponse.of(publicList, page, size, pageResult.getTotalElements());
    }

    public PaginatedResponse<PublicSentenceRuResponse> getSentences(String name, SentenceRu.Language lang, String tags, Integer term, Integer week, String difficultyLevel, int page, int size, String sort, String direction) {
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        Page<SentenceRuResponse> pageResult = sentenceRuService.getSentences(name, lang, tags, true, pageable);

        List<PublicSentenceRuResponse> publicList = pageResult.getContent().stream().map(r ->
            PublicSentenceRuResponse.builder()
                .id(r.getId())
                .name(r.getName())
                .phonetic(r.getPhonetic())
                .translation(r.getTranslation())
                .explanation(r.getExplanation())
                .term(r.getTerm())
                .week(r.getWeek())
                .tags(r.getTags())
                .difficultyLevel(r.getDifficultyLevel())
                .lang(r.getLang())
                .displayOrder(r.getDisplayOrder())
                .updatedAt(r.getUpdatedAt() != null ? r.getUpdatedAt().toString() : null)
                .build()
        ).toList();

        return PaginatedResponse.of(publicList, page, size, pageResult.getTotalElements());
    }

    public PaginatedResponse<PublicMultipleChoiceQuestionRuResponse> getMultipleChoiceQuestions(String lang, String difficultyLevel, String tags, Integer term, Integer week, int page, int size, String sort, String direction) {
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        Page<MultipleChoiceQuestionRuResponse> pageResult = multipleChoiceQuestionRuService.getAllMultipleChoiceQuestionRus(pageable, lang, difficultyLevel, tags, true);

        List<PublicMultipleChoiceQuestionRuResponse> publicList = pageResult.getContent().stream().map(r ->
            PublicMultipleChoiceQuestionRuResponse.builder()
                .id(r.getId())
                .question(r.getQuestion())
                .optionA(r.getOptionA())
                .optionB(r.getOptionB())
                .optionC(r.getOptionC())
                .optionD(r.getOptionD())
                .answer(r.getAnswer())
                .explanation(r.getExplanation())
                .difficultyLevel(r.getDifficultyLevel())
                .failCount(r.getFailCount())
                .successCount(r.getSuccessCount())
                .term(r.getTerm())
                .week(r.getWeek())
                .tags(r.getTags())
                .lang(r.getLang())
                .displayOrder(r.getDisplayOrder())
                .updatedAt(r.getUpdatedAt() != null ? r.getUpdatedAt().toString() : null)
                .build()
        ).toList();

        return PaginatedResponse.of(publicList, page, size, pageResult.getTotalElements());
    }

    public PaginatedResponse<PublicTrueFalseQuestionRuResponse> getTrueFalseQuestions(String lang, String difficultyLevel, String tags, Integer term, Integer week, int page, int size, String sort, String direction) {
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        Page<TrueFalseQuestionRuResponse> pageResult = trueFalseQuestionRuService.getAllTrueFalseQuestionRus(pageable, lang, difficultyLevel, tags, true);

        List<PublicTrueFalseQuestionRuResponse> publicList = pageResult.getContent().stream().map(r ->
            PublicTrueFalseQuestionRuResponse.builder()
                .id(r.getId())
                .question(r.getQuestion())
                .answer(r.getAnswer())
                .explanation(r.getExplanation())
                .difficultyLevel(r.getDifficultyLevel())
                .failCount(r.getFailCount())
                .successCount(r.getSuccessCount())
                .term(r.getTerm())
                .week(r.getWeek())
                .tags(r.getTags())
                .lang(r.getLang())
                .displayOrder(r.getDisplayOrder())
                .updatedAt(r.getUpdatedAt() != null ? r.getUpdatedAt().toString() : null)
                .build()
        ).toList();

        return PaginatedResponse.of(publicList, page, size, pageResult.getTotalElements());
    }

    public PaginatedResponse<PublicFreeTextQuestionRuResponse> getFreeTextQuestions(String lang, String difficultyLevel, String tags, Integer term, Integer week, int page, int size, String sort, String direction) {
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        Page<FreeTextQuestionRuResponse> pageResult = freeTextQuestionRuService.getAllFreeTextQuestionRus(pageable, lang, difficultyLevel, tags, true);

        List<PublicFreeTextQuestionRuResponse> publicList = pageResult.getContent().stream().map(r ->
            PublicFreeTextQuestionRuResponse.builder()
                .id(r.getId())
                .question(r.getQuestion())
                .answer(r.getAnswer())
                .explanation(r.getExplanation())
                .difficultyLevel(r.getDifficultyLevel())
                .term(r.getTerm())
                .week(r.getWeek())
                .tags(r.getTags())
                .lang(r.getLang())
                .displayOrder(r.getDisplayOrder())
                .updatedAt(r.getUpdatedAt() != null ? r.getUpdatedAt().toString() : null)
                .build()
        ).toList();

        return PaginatedResponse.of(publicList, page, size, pageResult.getTotalElements());
    }

    public PaginatedResponse<PublicFillBlankQuestionRuResponse> getFillBlankQuestions(String lang, String difficultyLevel, String tags, Integer term, Integer week, int page, int size, String sort, String direction) {
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        Page<FillBlankQuestionRuResponse> pageResult = fillBlankQuestionRuService.getAllFillBlankQuestionRus(pageable, lang, difficultyLevel, tags, true);

        List<PublicFillBlankQuestionRuResponse> publicList = pageResult.getContent().stream().map(r ->
            PublicFillBlankQuestionRuResponse.builder()
                .id(r.getId())
                .question(r.getQuestion())
                .answer(r.getAnswer())
                .explanation(r.getExplanation())
                .difficultyLevel(r.getDifficultyLevel())
                .failCount(r.getFailCount())
                .successCount(r.getSuccessCount())
                .term(r.getTerm())
                .week(r.getWeek())
                .tags(r.getTags())
                .lang(r.getLang())
                .displayOrder(r.getDisplayOrder())
                .updatedAt(r.getUpdatedAt() != null ? r.getUpdatedAt().toString() : null)
                .build()
        ).toList();

        return PaginatedResponse.of(publicList, page, size, pageResult.getTotalElements());
    }

    public PaginatedResponse<PublicArticleRuResponse> getArticles(String title, String lang, String tags, Integer term, Integer week, int page, int size, String sort, String direction) {
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        ArticleRu.Language language = lang != null ? ArticleRu.Language.valueOf(lang.toUpperCase()) : null;
        Page<ArticleRuResponse> pageResult = articleRuService.searchArticles(title, language, tags, true, pageable);

        List<PublicArticleRuResponse> publicList = pageResult.getContent().stream().map(r ->
            PublicArticleRuResponse.builder()
                .id(r.getId())
                .title(r.getTitle())
                .summary(r.getSummary())
                .content(r.getContent())
                .originalUrl(r.getOriginalUrl())
                .sourceName(r.getSourceName())
                .coverImageFilename(r.getCoverImageFilename())
                .coverImageFileUrl(r.getCoverImageFileUrl())
                .coverImageOriginalUrl(r.getCoverImageOriginalUrl())
                .term(r.getTerm())
                .week(r.getWeek())
                .tags(r.getTags())
                .lang(r.getLang())
                .displayOrder(r.getDisplayOrder())
                .updatedAt(r.getUpdatedAt() != null ? r.getUpdatedAt().toString() : null)
                .build()
        ).toList();

        return PaginatedResponse.of(publicList, page, size, pageResult.getTotalElements());
    }

    public PaginatedResponse<PublicAudioRuResponse> getAudios(String name, String lang, String tags, Integer term, Integer week, int page, int size, String sort, String direction) {
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        AudioRu.Language language = lang != null ? AudioRu.Language.valueOf(lang.toUpperCase()) : null;
        Page<AudioRuResponse> pageResult = audioRuService.searchAudios(name, language, tags, true, pageable);

        List<PublicAudioRuResponse> publicList = pageResult.getContent().stream().map(r ->
            PublicAudioRuResponse.builder()
                .id(r.getId())
                .name(r.getName())
                .filename(r.getFilename())
                .fileUrl(r.getFileUrl())
                .sizeBytes(r.getSizeBytes())
                .coverImageFilename(r.getCoverImageFilename())
                .coverImageFileUrl(r.getCoverImageFileUrl())
                .originalUrl(r.getOriginalUrl())
                .sourceName(r.getSourceName())
                .description(r.getDescription())
                .subtitle(r.getSubtitle())
                .artist(r.getArtist())
                .term(r.getTerm())
                .week(r.getWeek())
                .tags(r.getTags())
                .lang(r.getLang())
                .displayOrder(r.getDisplayOrder())
                .updatedAt(r.getUpdatedAt() != null ? r.getUpdatedAt().toString() : null)
                .build()
        ).toList();

        return PaginatedResponse.of(publicList, page, size, pageResult.getTotalElements());
    }

    public PaginatedResponse<PublicImageRuResponse> getImages(String name, String lang, String tags, Integer term, Integer week, int page, int size, String sort, String direction) {
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        ImageRu.Language language = lang != null ? ImageRu.Language.valueOf(lang.toUpperCase()) : null;
        Page<ImageRuResponse> pageResult = imageRuService.searchImages(name, language, tags, true, pageable);

        List<PublicImageRuResponse> publicList = pageResult.getContent().stream().map(r ->
            PublicImageRuResponse.builder()
                .id(r.getId())
                .name(r.getName())
                .originalUrl(r.getOriginalUrl())
                .sourceName(r.getSourceName())
                .filename(r.getFilename())
                .fileUrl(r.getFileUrl())
                .thumbnailFilename(r.getThumbnailFilename())
                .thumbnailFileUrl(r.getThumbnailFileUrl())
                .extension(r.getExtension())
                .mimeType(r.getMimeType())
                .sizeBytes(r.getSizeBytes())
                .width(r.getWidth())
                .height(r.getHeight())
                .altText(r.getAltText())
                .term(r.getTerm())
                .week(r.getWeek())
                .tags(r.getTags())
                .lang(r.getLang())
                .displayOrder(r.getDisplayOrder())
                .updatedAt(r.getUpdatedAt() != null ? r.getUpdatedAt().toString() : null)
                .build()
        ).toList();

        return PaginatedResponse.of(publicList, page, size, pageResult.getTotalElements());
    }

    public PaginatedResponse<PublicVideoRuResponse> getVideos(String name, String lang, String tags, Integer term, Integer week, int page, int size, String sort, String direction) {
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        VideoRu.Language language = lang != null ? VideoRu.Language.valueOf(lang.toUpperCase()) : null;
        Page<VideoRuResponse> pageResult = videoRuService.searchVideos(name, language, tags, true, pageable);

        List<PublicVideoRuResponse> publicList = pageResult.getContent().stream().map(r ->
            PublicVideoRuResponse.builder()
                .id(r.getId())
                .name(r.getName())
                .filename(r.getFilename())
                .fileUrl(r.getFileUrl())
                .sizeBytes(r.getSizeBytes())
                .coverImageFilename(r.getCoverImageFilename())
                .coverImageFileUrl(r.getCoverImageFileUrl())
                .originalUrl(r.getOriginalUrl())
                .sourceName(r.getSourceName())
                .description(r.getDescription())
                .term(r.getTerm())
                .week(r.getWeek())
                .tags(r.getTags())
                .lang(r.getLang())
                .displayOrder(r.getDisplayOrder())
                .updatedAt(r.getUpdatedAt() != null ? r.getUpdatedAt().toString() : null)
                .build()
        ).toList();

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
