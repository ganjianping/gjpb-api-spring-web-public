package org.ganjp.blog.rubi.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.ganjp.blog.common.model.entity.BaseEntity;

/**
 * Fill-in-the-Blank Question Entity for Rubi
 */
@Entity
@Table(name = "rubi_fill_blank_question")
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FillBlankQuestionRu extends BaseEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "question", length = 500, nullable = false)
    private String question;

    @Column(name = "answer", length = 200, nullable = false)
    private String answer;

    @Column(name = "explanation", length = 1000)
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

    @Column(name = "grammar_chapter", length = 40)
    private String grammarChapter;

    @Column(name = "science_chapter", length = 40)
    private String scienceChapter;

    /**
     * Language enumeration
     */
    public enum Language {
        EN,
        ZH
    }

    /**
     * Check if FillBlankQuestion is active
     */
    public boolean isActiveFillBlankQuestionRu() {
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

    /**
     * Get answer as array
     */
    public String[] getAnswerArray() {
        if (answer == null || answer.trim().isEmpty()) {
            return new String[0];
        }
        return answer.split(",");
    }

    /**
     * Set answer from array
     */
    public void setAnswerFromArray(String[] answerArray) {
        if (answerArray == null || answerArray.length == 0) {
            this.answer = null;
        } else {
            this.answer = String.join(",", answerArray);
        }
    }
}
