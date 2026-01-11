package org.ganjp.blog.rubi.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.ganjp.blog.common.model.entity.BaseEntity;

/**
 * True/False Question Entity for Rubi
 */
@Entity
@Table(name = "rubi_true_false_question")
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrueFalseQuestionRu extends BaseEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "question", length = 500, nullable = false)
    private String question;

    @Enumerated(EnumType.STRING)
    @Column(name = "answer", nullable = false)
    private Answer answer;

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
     * Answer enumeration
     */
    public enum Answer {
        TRUE,
        FALSE
    }

    /**
     * Language enumeration
     */
    public enum Language {
        EN,
        ZH
    }

    /**
     * Check if TrueFalseQuestion is active
     */
    public boolean isActiveTrueFalseQuestionRu() {
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
