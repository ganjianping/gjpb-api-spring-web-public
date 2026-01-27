package org.ganjp.blog.rubi.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.ganjp.blog.common.model.entity.BaseEntity;

/**
 * Short Answer Question Entity for Rubi
 */
@Entity
@Table(name = "rubi_free_text_question")
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FreeTextQuestionRu extends BaseEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "question", length = 1000)
    private String question;

    @Column(name = "answer", length = 1000)
    private String answer;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "questiona", length = 500)
    private String questiona;

    @Column(name = "answera", length = 1000)
    private String answera;

    @Column(name = "questionb", length = 500)
    private String questionb;

    @Column(name = "answerb", length = 1000)
    private String answerb;

    @Column(name = "questionc", length = 500)
    private String questionc;

    @Column(name = "answerc", length = 1000)
    private String answerc;

    @Column(name = "questiond", length = 500)
    private String questiond;

    @Column(name = "answerd", length = 1000)
    private String answerd;

    @Column(name = "questione", length = 500)
    private String questione;

    @Column(name = "answere", length = 1000)
    private String answere;

    @Column(name = "questionf", length = 500)
    private String questionf;

    @Column(name = "answerf", length = 1000)
    private String answerf;

    @Column(name = "explanation", length = 2000)
    private String explanation;

    @Column(name = "difficulty_level", length = 20)
    private String difficultyLevel;

    @Column(name = "fail_count", nullable = false)
    @Builder.Default
    private Integer failCount = 0;

    @Column(name = "success_count", nullable = false)
    @Builder.Default
    private Integer successCount = 0;

    @Column(name = "term")
    private Integer term;

    @Column(name = "week")
    private Integer week;

    @Column(name = "tags", length = 500)
    private String tags;

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
     * Check if FreeTextQuestion is active
     */
    public boolean isActiveFreeTextQuestionRu() {
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