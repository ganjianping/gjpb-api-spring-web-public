package org.ganjp.blog.rubi.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.ganjp.blog.common.model.entity.BaseEntity;

/**
 * Vocabulary Entity for Rubi
 */
@Entity
@Table(name = "rubi_vocabulary")
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VocabularyRu extends BaseEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "phonetic", length = 100)
    private String phonetic;

    @Column(name = "part_of_speech", length = 50)
    private String partOfSpeech;

    @Column(name = "noun_plural_form", length = 100)
    private String nounPluralForm;

    @Column(name = "noun_form", length = 100)
    private String nounForm;

    @Column(name = "noun_meaning", length = 100)
    private String nounMeaning;

    @Column(name = "noun_example", length = 500)
    private String nounExample;

    @Column(name = "verb_simple_past_tense", length = 100)
    private String verbSimplePastTense;

    @Column(name = "verb_past_perfect_tense", length = 100)
    private String verbPastPerfectTense;

    @Column(name = "verb_present_participle", length = 100)
    private String verbPresentParticiple;

    @Column(name = "adjective_comparative_form", length = 100)
    private String adjectiveComparativeForm;

    @Column(name = "adjective_superlative_form", length = 100)
    private String adjectiveSuperlativeForm;

    @Column(name = "verb_form", length = 100)
    private String verbForm;

    @Column(name = "verb_meaning", length = 100)
    private String verbMeaning;

    @Column(name = "verb_example", length = 500)
    private String verbExample;

    @Column(name = "adjective_form", length = 100)
    private String adjectiveForm;

    @Column(name = "adjective_meaning", length = 100)
    private String adjectiveMeaning;

    @Column(name = "adjective_example", length = 500)
    private String adjectiveExample;

    @Column(name = "adverb_form", length = 100)
    private String adverbForm;

    @Column(name = "adverb_meaning", length = 100)
    private String adverbMeaning;

    @Column(name = "adverb_example", length = 500)
    private String adverbExample;

    @Column(name = "translation", length = 500)
    private String translation;

    @Column(name = "synonyms", length = 200)
    private String synonyms;

    @Column(name = "definition", length = 2000)
    private String definition;

    @Column(name = "example", length = 2000)
    private String example;

    @Column(name = "dictionary_url", length = 500)
    private String dictionaryUrl;

    @Column(name = "image_filename", length = 100)
    private String imageFilename;

    @Column(name = "image_original_url", length = 500)
    private String imageOriginalUrl;

    @Column(name = "phonetic_audio_filename", length = 100)
    private String phoneticAudioFilename;

    @Column(name = "phonetic_audio_original_url", length = 500)
    private String phoneticAudioOriginalUrl;

    @Column(name = "term")
    private Integer term;

    @Column(name = "week")
    private Integer week;

    @Column(name = "tags", length = 500)
    private String tags;

    @Column(name = "difficulty_level", length = 20)
    private String difficultyLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "lang", nullable = false)
    @Builder.Default
    private Language lang = Language.EN;

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /**
     * Language enumeration
     */
    public enum Language {
        EN,
        ZH
    }

    /**
     * Check if vocabulary is active
     */
    public boolean isActiveVocabulary() {
        return isActive != null && isActive;
    }

    /**
     * Get tags as array
     */
    public String[] getTagsArray() {
        if (tags == null || tags.trim().isEmpty()) {
            return new String[0];
        }
        return tags.split(",");
    }

    /**
     * Set tags from array
     */
    public void setTagsFromArray(String[] tagsArray) {
        if (tagsArray == null || tagsArray.length == 0) {
            this.tags = null;
        } else {
            this.tags = String.join(",", tagsArray);
        }
    }
}
