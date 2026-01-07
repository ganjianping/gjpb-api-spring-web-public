package org.ganjp.blog.rubi.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.ganjp.blog.common.model.entity.BaseEntity;

/**
 * Multiple Choice Question Entity for Rubi
 */
@Entity
@Table(name = "rubi_mcq")
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Mcq extends BaseEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "question", length = 1000, nullable = false)
    private String question;

    @Column(name = "option_a", length = 200)
    private String optionA;

    @Column(name = "option_b", length = 200)
    private String optionB;

    @Column(name = "option_c", length = 200)
    private String optionC;

    @Column(name = "option_d", length = 200)
    private String optionD;

    @Column(name = "correct_answers", length = 10, nullable = false)
    private String correctAnswers;

    @Column(name = "is_multiple_correct", nullable = false)
    @Builder.Default
    private Boolean isMultipleCorrect = false;

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
     * Check if MCQ is active
     */
    public boolean isActiveMcq() {
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
     * Get correct answers as array
     */
    public String[] getCorrectAnswersArray() {
        if (correctAnswers == null || correctAnswers.trim().isEmpty()) {
            return new String[0];
        }
        return correctAnswers.split(",");
    }

    /**
     * Set correct answers from array
     */
    public void setCorrectAnswersFromArray(String[] correctAnswersArray) {
        if (correctAnswersArray == null || correctAnswersArray.length == 0) {
            this.correctAnswers = null;
        } else {
            this.correctAnswers = String.join(",", correctAnswersArray);
        }
    }
}