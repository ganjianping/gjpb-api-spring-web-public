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

    @Column(name = "word", length = 100, nullable = false)
    private String word;

    @Column(name = "word_image_filename", length = 100)
    private String wordImageFilename;

    @Column(name = "word_image_original_url", length = 500)
    private String wordImageOriginalUrl;

    @Column(name = "simple_past_tense", length = 100)
    private String simplePastTense;

    @Column(name = "past_perfect_tense", length = 100)
    private String pastPerfectTense;

    @Column(name = "translation", length = 500)
    private String translation;

    @Column(name = "synonyms", length = 200)
    private String synonyms;

    @Column(name = "plural_form", length = 100)
    private String pluralForm;

    @Column(name = "phonetic", length = 100)
    private String phonetic;

    @Column(name = "phonetic_audio_filename", length = 100)
    private String phoneticAudioFilename;

    @Column(name = "phonetic_audio_original_url", length = 500)
    private String phoneticAudioOriginalUrl;

    @Column(name = "part_of_speech", length = 50)
    private String partOfSpeech;

    @Column(name = "definition", length = 2000, nullable = false)
    private String definition;

    @Column(name = "example", length = 2000)
    private String example;

    @Column(name = "dictionary_url", length = 500)
    private String dictionaryUrl;

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
