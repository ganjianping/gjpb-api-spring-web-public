package org.ganjp.blog.rubi.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Entity
@Table(name = "rubi_question_image")
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionImageRu {
    @Id
    @Column(length = 36, nullable = false)
    private String id;

    @Column(name = "multiple_choice_question_id", length = 36)
    private String multipleChoiceQuestionId;

    @Column(name = "free_text_question_id", length = 36)
    private String freeTextQuestionId;

    @Column(name = "true_false_question_id", length = 36)
    private String trueFalseQuestionId;

    @Column(name = "fill_blank_question_id", length = 36)
    private String fillBlankQuestionId;

    @Column(length = 255, nullable = false)
    private String filename;

    @Column(name = "original_url", length = 500)
    private String originalUrl;

    @Column(name = "width")
    private Integer width;

    @Column(name = "height")
    private Integer height;

    @Enumerated(EnumType.STRING)
    @Column(length = 2, nullable = false)
    @Builder.Default
    private Language lang = Language.EN;

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(name = "created_at", nullable = false)
    private Timestamp createdAt;

    @Column(name = "updated_at", nullable = false)
    private Timestamp updatedAt;

    @Column(name = "created_by", length = 36)
    private String createdBy;

    @Column(name = "updated_by", length = 36)
    private String updatedBy;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    public enum Language {
        EN, ZH
    }
}
