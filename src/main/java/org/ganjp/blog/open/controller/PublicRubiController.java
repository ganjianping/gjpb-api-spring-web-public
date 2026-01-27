package org.ganjp.blog.open.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ganjp.blog.common.model.ApiResponse;
import org.ganjp.blog.common.model.PaginatedResponse;
import org.ganjp.blog.open.model.*;
import org.ganjp.blog.open.service.PublicRubiService;
import org.ganjp.blog.rubi.model.entity.*;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

@RestController
@RequestMapping("/v1/public")
@RequiredArgsConstructor
@Slf4j
public class PublicRubiController {
    private final PublicRubiService publicRubiService;

    /**
     * Validates and parses language parameter to enum.
     * Returns null if lang is null or blank.
     * @param lang the language string parameter
     * @param enumClass the enum class to convert to
     * @return the parsed Language enum, or null if input is null/blank
     * @throws IllegalArgumentException if lang value is invalid
     */
    private <T extends Enum<T>> T validateAndParseLanguage(String lang, Class<T> enumClass) {
        if (lang == null || lang.isBlank()) {
            return null;
        }
        return Enum.valueOf(enumClass, lang.toUpperCase(Locale.ROOT));
    }

    @GetMapping("/vocabulary-rus")
    public ApiResponse<PaginatedResponse<PublicVocabularyRuResponse>> getVocabularies(@RequestParam(required = false) String name,
                                                             @RequestParam(required = false) String lang,
                                                             @RequestParam(required = false) String tags,
                                                             @RequestParam(required = false) Integer term,
                                                             @RequestParam(required = false) Integer week,
                                                             @RequestParam(required = false) String difficultyLevel,
                                                             @RequestParam(required = false) String partOfSpeech,
                                                             @RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "20") int size,
                                                             @RequestParam(defaultValue = "displayOrder") String sort,
                                                             @RequestParam(defaultValue = "asc") String direction) {
        VocabularyRu.Language langEnum;
        try {
            langEnum = validateAndParseLanguage(lang, VocabularyRu.Language.class);
        } catch (IllegalArgumentException ex) {
            return ApiResponse.error(400, "Invalid lang", null);
        }
        var resp = publicRubiService.getVocabularies(name, langEnum, tags, term, week, difficultyLevel, partOfSpeech, page, size, sort, direction);
        return ApiResponse.success(resp, "Vocabularies retrieved");
    }

    @GetMapping("/expression-rus")
    public ApiResponse<PaginatedResponse<PublicExpressionRuResponse>> getExpressions(@RequestParam(required = false) String name,
                                                             @RequestParam(required = false) String lang,
                                                             @RequestParam(required = false) String tags,
                                                             @RequestParam(required = false) Integer term,
                                                             @RequestParam(required = false) Integer week,
                                                             @RequestParam(required = false) String difficultyLevel,
                                                             @RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "20") int size,
                                                             @RequestParam(defaultValue = "displayOrder") String sort,
                                                             @RequestParam(defaultValue = "asc") String direction) {
        ExpressionRu.Language langEnum;
        try {
            langEnum = validateAndParseLanguage(lang, ExpressionRu.Language.class);
        } catch (IllegalArgumentException ex) {
            return ApiResponse.error(400, "Invalid lang", null);
        }
        var resp = publicRubiService.getExpressions(name, langEnum, tags, term, week, difficultyLevel, page, size, sort, direction);
        return ApiResponse.success(resp, "Expressions retrieved");
    }

    @GetMapping("/sentence-rus")
    public ApiResponse<PaginatedResponse<PublicSentenceRuResponse>> getSentences(@RequestParam(required = false) String name,
                                                             @RequestParam(required = false) String lang,
                                                             @RequestParam(required = false) String tags,
                                                             @RequestParam(required = false) Integer term,
                                                             @RequestParam(required = false) Integer week,
                                                             @RequestParam(required = false) String difficultyLevel,
                                                             @RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "20") int size,
                                                             @RequestParam(defaultValue = "displayOrder") String sort,
                                                             @RequestParam(defaultValue = "asc") String direction) {
        SentenceRu.Language langEnum;
        try {
            langEnum = validateAndParseLanguage(lang, SentenceRu.Language.class);
        } catch (IllegalArgumentException ex) {
            return ApiResponse.error(400, "Invalid lang", null);
        }
        var resp = publicRubiService.getSentences(name, langEnum, tags, term, week, difficultyLevel, page, size, sort, direction);
        return ApiResponse.success(resp, "Sentences retrieved");
    }

    @GetMapping("/multiple-choice-question-rus")
    public ApiResponse<PaginatedResponse<PublicMultipleChoiceQuestionRuResponse>> getMultipleChoiceQuestions(
                                                             @RequestParam(required = false) String lang,
                                                             @RequestParam(required = false) String tags,
                                                             @RequestParam(required = false) Integer term,
                                                             @RequestParam(required = false) Integer week,
                                                             @RequestParam(required = false) String difficultyLevel,
                                                             @RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "20") int size,
                                                             @RequestParam(defaultValue = "displayOrder") String sort,
                                                             @RequestParam(defaultValue = "asc") String direction) {
        var resp = publicRubiService.getMultipleChoiceQuestions(lang, difficultyLevel, tags, term, week, page, size, sort, direction);
        return ApiResponse.success(resp, "Multiple choice questions retrieved");
    }

    @GetMapping("/true-false-question-rus")
    public ApiResponse<PaginatedResponse<PublicTrueFalseQuestionRuResponse>> getTrueFalseQuestions(
                                                             @RequestParam(required = false) String lang,
                                                             @RequestParam(required = false) String tags,
                                                             @RequestParam(required = false) Integer term,
                                                             @RequestParam(required = false) Integer week,
                                                             @RequestParam(required = false) String difficultyLevel,
                                                             @RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "20") int size,
                                                             @RequestParam(defaultValue = "displayOrder") String sort,
                                                             @RequestParam(defaultValue = "asc") String direction) {
        var resp = publicRubiService.getTrueFalseQuestions(lang, difficultyLevel, tags, term, week, page, size, sort, direction);
        return ApiResponse.success(resp, "True/False questions retrieved");
    }

    @GetMapping("/free-text-question-rus")
    public ApiResponse<PaginatedResponse<PublicFreeTextQuestionRuResponse>> getFreeTextQuestions(
                                                             @RequestParam(required = false) String lang,
                                                             @RequestParam(required = false) String tags,
                                                             @RequestParam(required = false) Integer term,
                                                             @RequestParam(required = false) Integer week,
                                                             @RequestParam(required = false) String difficultyLevel,
                                                             @RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "20") int size,
                                                             @RequestParam(defaultValue = "displayOrder") String sort,
                                                             @RequestParam(defaultValue = "asc") String direction) {
        var resp = publicRubiService.getFreeTextQuestions(lang, difficultyLevel, tags, term, week, page, size, sort, direction);
        return ApiResponse.success(resp, "Free text questions retrieved");
    }

    @GetMapping("/fill-blank-question-rus")
    public ApiResponse<PaginatedResponse<PublicFillBlankQuestionRuResponse>> getFillBlankQuestions(
                                                             @RequestParam(required = false) String lang,
                                                             @RequestParam(required = false) String tags,
                                                             @RequestParam(required = false) Integer term,
                                                             @RequestParam(required = false) Integer week,
                                                             @RequestParam(required = false) String difficultyLevel,
                                                             @RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "20") int size,
                                                             @RequestParam(defaultValue = "displayOrder") String sort,
                                                             @RequestParam(defaultValue = "asc") String direction) {
        var resp = publicRubiService.getFillBlankQuestions(lang, difficultyLevel, tags, term, week, page, size, sort, direction);
        return ApiResponse.success(resp, "Fill blank questions retrieved");
    }

    @GetMapping("/article-rus")
    public ApiResponse<PaginatedResponse<PublicArticleRuResponse>> getArticles(
                                                             @RequestParam(required = false) String title,
                                                             @RequestParam(required = false) String lang,
                                                             @RequestParam(required = false) String tags,
                                                             @RequestParam(required = false) Integer term,
                                                             @RequestParam(required = false) Integer week,
                                                             @RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "20") int size,
                                                             @RequestParam(defaultValue = "displayOrder") String sort,
                                                             @RequestParam(defaultValue = "asc") String direction) {
        var resp = publicRubiService.getArticles(title, lang, tags, term, week, page, size, sort, direction);
        return ApiResponse.success(resp, "Articles retrieved");
    }

    @GetMapping("/article-rus/{id}")
    public ApiResponse<PublicArticleRuResponse> getArticleById(@PathVariable String id) {
        var resp = publicRubiService.getArticleById(id);
        return ApiResponse.success(resp, "Article retrieved");
    }

    @GetMapping("/audio-rus")
    public ApiResponse<PaginatedResponse<PublicAudioRuResponse>> getAudios(
                                                             @RequestParam(required = false) String name,
                                                             @RequestParam(required = false) String lang,
                                                             @RequestParam(required = false) String tags,
                                                             @RequestParam(required = false) Integer term,
                                                             @RequestParam(required = false) Integer week,
                                                             @RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "20") int size,
                                                             @RequestParam(defaultValue = "displayOrder") String sort,
                                                             @RequestParam(defaultValue = "asc") String direction) {
        var resp = publicRubiService.getAudios(name, lang, tags, term, week, page, size, sort, direction);
        return ApiResponse.success(resp, "Audios retrieved");
    }

    @GetMapping("/image-rus")
    public ApiResponse<PaginatedResponse<PublicImageRuResponse>> getImages(
                                                             @RequestParam(required = false) String name,
                                                             @RequestParam(required = false) String lang,
                                                             @RequestParam(required = false) String tags,
                                                             @RequestParam(required = false) Integer term,
                                                             @RequestParam(required = false) Integer week,
                                                             @RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "20") int size,
                                                             @RequestParam(defaultValue = "displayOrder") String sort,
                                                             @RequestParam(defaultValue = "asc") String direction) {
        var resp = publicRubiService.getImages(name, lang, tags, term, week, page, size, sort, direction);
        return ApiResponse.success(resp, "Images retrieved");
    }

    @GetMapping("/video-rus")
    public ApiResponse<PaginatedResponse<PublicVideoRuResponse>> getVideos(
                                                             @RequestParam(required = false) String name,
                                                             @RequestParam(required = false) String lang,
                                                             @RequestParam(required = false) String tags,
                                                             @RequestParam(required = false) Integer term,
                                                             @RequestParam(required = false) Integer week,
                                                             @RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "20") int size,
                                                             @RequestParam(defaultValue = "displayOrder") String sort,
                                                             @RequestParam(defaultValue = "asc") String direction) {
        var resp = publicRubiService.getVideos(name, lang, tags, term, week, page, size, sort, direction);
        return ApiResponse.success(resp, "Videos retrieved");
    }

    @PutMapping("/multiple-choice-question-rus/{id}/success")
    public ApiResponse<Void> incrementMultipleChoiceQuestionSuccess(@PathVariable String id) {
        publicRubiService.incrementMultipleChoiceQuestionSuccessCount(id);
        return ApiResponse.success(null, "Success count incremented");
    }

    @PutMapping("/multiple-choice-question-rus/{id}/fail")
    public ApiResponse<Void> incrementMultipleChoiceQuestionFail(@PathVariable String id) {
        publicRubiService.incrementMultipleChoiceQuestionFailCount(id);
        return ApiResponse.success(null, "Fail count incremented");
    }

    @PutMapping("/true-false-question-rus/{id}/success")
    public ApiResponse<Void> incrementTrueFalseQuestionSuccess(@PathVariable String id) {
        publicRubiService.incrementTrueFalseQuestionSuccessCount(id);
        return ApiResponse.success(null, "Success count incremented");
    }

    @PutMapping("/true-false-question-rus/{id}/fail")
    public ApiResponse<Void> incrementTrueFalseQuestionFail(@PathVariable String id) {
        publicRubiService.incrementTrueFalseQuestionFailCount(id);
        return ApiResponse.success(null, "Fail count incremented");
    }

    @PutMapping("/fill-blank-question-rus/{id}/success")
    public ApiResponse<Void> incrementFillBlankQuestionSuccess(@PathVariable String id) {
        publicRubiService.incrementFillBlankQuestionSuccessCount(id);
        return ApiResponse.success(null, "Success count incremented");
    }

    @PutMapping("/fill-blank-question-rus/{id}/fail")
    public ApiResponse<Void> incrementFillBlankQuestionFail(@PathVariable String id) {
        publicRubiService.incrementFillBlankQuestionFailCount(id);
        return ApiResponse.success(null, "Fail count incremented");
    }

    @PutMapping("/free-text-question-rus/{id}/success")
    public ApiResponse<Void> incrementFreeTextQuestionSuccess(@PathVariable String id) {
        publicRubiService.incrementFreeTextQuestionSuccessCount(id);
        return ApiResponse.success(null, "Success count incremented");
    }

    @PutMapping("/free-text-question-rus/{id}/fail")
    public ApiResponse<Void> incrementFreeTextQuestionFail(@PathVariable String id) {
        publicRubiService.incrementFreeTextQuestionFailCount(id);
        return ApiResponse.success(null, "Fail count incremented");
    }
}
