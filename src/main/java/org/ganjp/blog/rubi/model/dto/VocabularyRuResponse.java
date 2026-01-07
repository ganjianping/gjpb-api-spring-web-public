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
    private String word;
    private String wordImageFilename;
    private String wordImageOriginalUrl;
    private String simplePastTense;
    private String pastPerfectTense;
    private String translation;
    private String synonyms;
    private String pluralForm;
    private String phonetic;
    private String phoneticAudioFilename;
    private String phoneticAudioOriginalUrl;
    private String partOfSpeech;
    private String definition;
    private String example;
    private String dictionaryUrl;
    private String tags;
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
                .word(vocabulary.getWord())
                .wordImageFilename(vocabulary.getWordImageFilename())
                .wordImageOriginalUrl(vocabulary.getWordImageOriginalUrl())
                .simplePastTense(vocabulary.getSimplePastTense())
                .pastPerfectTense(vocabulary.getPastPerfectTense())
                .translation(vocabulary.getTranslation())
                .synonyms(vocabulary.getSynonyms())
                .pluralForm(vocabulary.getPluralForm())
                .phonetic(vocabulary.getPhonetic())
                .phoneticAudioFilename(vocabulary.getPhoneticAudioFilename())
                .phoneticAudioOriginalUrl(vocabulary.getPhoneticAudioOriginalUrl())
                .partOfSpeech(vocabulary.getPartOfSpeech())
                .definition(vocabulary.getDefinition())
                .example(vocabulary.getExample())
                .dictionaryUrl(vocabulary.getDictionaryUrl())
                .tags(vocabulary.getTags())
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
