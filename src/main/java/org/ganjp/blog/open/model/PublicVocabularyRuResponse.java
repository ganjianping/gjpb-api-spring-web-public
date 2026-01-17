package org.ganjp.blog.open.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ganjp.blog.rubi.model.entity.VocabularyRu;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicVocabularyRuResponse {
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

    private String imageUrl;
    private String phoneticAudioUrl;

    private Integer term;
    private Integer week;
    private String tags;
    private String difficultyLevel;
    private VocabularyRu.Language lang;
    private Integer displayOrder;
    private String updatedAt;
}
