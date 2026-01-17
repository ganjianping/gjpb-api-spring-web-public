package org.ganjp.blog.rubi.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ganjp.blog.rubi.model.entity.VocabularyRu;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VocabularyRuResponse {

    private String id;
    private String name;
    private String phonetic;
    private String partOfSpeech;

    private String nounPluralForm;
    private String nounForm;
    private String nounMeaning;
    private String nounExample;
    private String verbSimplePastTense;
    private String verbPastPerfectTense;
    private String verbPresentParticiple;
    private String adjectiveComparativeForm;
    private String adjectiveSuperlativeForm;

    private String verbForm;
    private String verbMeaning;
    private String verbExample;
    private String adjectiveForm;
    private String adjectiveMeaning;
    private String adjectiveExample;
    private String adverbForm;
    private String adverbMeaning;
    private String adverbExample;

    private String translation;
    private String synonyms;
    private String definition;
    private String example;
    private String dictionaryUrl;

    private String imageFilename;
    private String imageUrl;
    private String imageOriginalUrl;

    private String phoneticAudioFilename;
    private String phoneticAudioUrl;
    private String phoneticAudioOriginalUrl;

    private Integer term;
    private Integer week;
    private String tags;
    private String difficultyLevel;
    private VocabularyRu.Language lang;
    private Integer displayOrder;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    public static VocabularyRuResponse fromEntity(VocabularyRu vocabulary) {
        if (vocabulary == null) {
            return null;
        }
        return VocabularyRuResponse.builder()
                .id(vocabulary.getId())
                .name(vocabulary.getName())
                .phonetic(vocabulary.getPhonetic())
                .partOfSpeech(vocabulary.getPartOfSpeech())
                .nounPluralForm(vocabulary.getNounPluralForm())
                .nounForm(vocabulary.getNounForm())
                .nounMeaning(vocabulary.getNounMeaning())
                .nounExample(vocabulary.getNounExample())
                .verbSimplePastTense(vocabulary.getVerbSimplePastTense())
                .verbPastPerfectTense(vocabulary.getVerbPastPerfectTense())
                .verbPresentParticiple(vocabulary.getVerbPresentParticiple())
                .adjectiveComparativeForm(vocabulary.getAdjectiveComparativeForm())
                .adjectiveSuperlativeForm(vocabulary.getAdjectiveSuperlativeForm())
                .verbForm(vocabulary.getVerbForm())
                .verbMeaning(vocabulary.getVerbMeaning())
                .verbExample(vocabulary.getVerbExample())
                .adjectiveForm(vocabulary.getAdjectiveForm())
                .adjectiveMeaning(vocabulary.getAdjectiveMeaning())
                .adjectiveExample(vocabulary.getAdjectiveExample())
                .adverbForm(vocabulary.getAdverbForm())
                .adverbMeaning(vocabulary.getAdverbMeaning())
                .adverbExample(vocabulary.getAdverbExample())
                .translation(vocabulary.getTranslation())
                .synonyms(vocabulary.getSynonyms())
                .definition(vocabulary.getDefinition())
                .example(vocabulary.getExample())
                .dictionaryUrl(vocabulary.getDictionaryUrl())
                .imageFilename(vocabulary.getImageFilename())
                .imageOriginalUrl(vocabulary.getImageOriginalUrl())
                .phoneticAudioFilename(vocabulary.getPhoneticAudioFilename())
                .phoneticAudioOriginalUrl(vocabulary.getPhoneticAudioOriginalUrl())
                .term(vocabulary.getTerm())
                .week(vocabulary.getWeek())
                .tags(vocabulary.getTags())
                .difficultyLevel(vocabulary.getDifficultyLevel())
                .lang(vocabulary.getLang())
                .displayOrder(vocabulary.getDisplayOrder())
                .isActive(vocabulary.getIsActive())
                .createdAt(vocabulary.getCreatedAt())
                .updatedAt(vocabulary.getUpdatedAt())
                .createdBy(vocabulary.getCreatedBy())
                .updatedBy(vocabulary.getUpdatedBy())
                .build();
    }

    public static VocabularyRuResponse fromEntity(VocabularyRu vocabulary, String vocabularyBaseUrl) {
        if (vocabulary == null) {
            return null;
        }
        return VocabularyRuResponse.builder()
                .id(vocabulary.getId())
                .name(vocabulary.getName())
                .phonetic(vocabulary.getPhonetic())
                .partOfSpeech(vocabulary.getPartOfSpeech())
                .nounPluralForm(vocabulary.getNounPluralForm())
                .nounForm(vocabulary.getNounForm())
                .nounMeaning(vocabulary.getNounMeaning())
                .nounExample(vocabulary.getNounExample())
                .verbSimplePastTense(vocabulary.getVerbSimplePastTense())
                .verbPastPerfectTense(vocabulary.getVerbPastPerfectTense())
                .verbPresentParticiple(vocabulary.getVerbPresentParticiple())
                .adjectiveComparativeForm(vocabulary.getAdjectiveComparativeForm())
                .adjectiveSuperlativeForm(vocabulary.getAdjectiveSuperlativeForm())
                .verbForm(vocabulary.getVerbForm())
                .verbMeaning(vocabulary.getVerbMeaning())
                .verbExample(vocabulary.getVerbExample())
                .adjectiveForm(vocabulary.getAdjectiveForm())
                .adjectiveMeaning(vocabulary.getAdjectiveMeaning())
                .adjectiveExample(vocabulary.getAdjectiveExample())
                .adverbForm(vocabulary.getAdverbForm())
                .adverbMeaning(vocabulary.getAdverbMeaning())
                .adverbExample(vocabulary.getAdverbExample())
                .translation(vocabulary.getTranslation())
                .synonyms(vocabulary.getSynonyms())
                .definition(vocabulary.getDefinition())
                .example(vocabulary.getExample())
                .dictionaryUrl(vocabulary.getDictionaryUrl())
                .imageFilename(vocabulary.getImageFilename())
                .imageUrl(vocabulary.getImageFilename() != null && vocabularyBaseUrl != null ?
                    vocabularyBaseUrl + "/images/" + vocabulary.getImageFilename() : null)
                .imageOriginalUrl(vocabulary.getImageOriginalUrl())
                .phoneticAudioFilename(vocabulary.getPhoneticAudioFilename())
                .phoneticAudioUrl(vocabulary.getPhoneticAudioFilename() != null && vocabularyBaseUrl != null ?
                    vocabularyBaseUrl + "/audios/" + vocabulary.getPhoneticAudioFilename() : null)
                .phoneticAudioOriginalUrl(vocabulary.getPhoneticAudioOriginalUrl())
                .term(vocabulary.getTerm())
                .week(vocabulary.getWeek())
                .tags(vocabulary.getTags())
                .difficultyLevel(vocabulary.getDifficultyLevel())
                .lang(vocabulary.getLang())
                .displayOrder(vocabulary.getDisplayOrder())
                .isActive(vocabulary.getIsActive())
                .createdAt(vocabulary.getCreatedAt())
                .updatedAt(vocabulary.getUpdatedAt())
                .createdBy(vocabulary.getCreatedBy())
                .updatedBy(vocabulary.getUpdatedBy())
                .build();
    }
}
