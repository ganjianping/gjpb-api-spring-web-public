package org.ganjp.blog.rubi.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ganjp.blog.rubi.model.entity.ExpressionRu;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpressionRuResponse {

    private String id;
    private String name;
    private String phonetic;
    private String translation;
    private String explanation;
    private String example;
    private Integer term;
    private Integer week;
    private String tags;
    private String difficultyLevel;
    private ExpressionRu.Language lang;
    private Integer displayOrder;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    public static ExpressionRuResponse fromEntity(ExpressionRu expression) {
        if (expression == null) {
            return null;
        }
        return ExpressionRuResponse.builder()
                .id(expression.getId())
                .name(expression.getName())
                .phonetic(expression.getPhonetic())
                .translation(expression.getTranslation())
                .explanation(expression.getExplanation())
                .example(expression.getExample())
                .term(expression.getTerm())
                .week(expression.getWeek())
                .tags(expression.getTags())
                .difficultyLevel(expression.getDifficultyLevel())
                .lang(expression.getLang())
                .displayOrder(expression.getDisplayOrder())
                .isActive(expression.getIsActive())
                .createdAt(expression.getCreatedAt())
                .updatedAt(expression.getUpdatedAt())
                .createdBy(expression.getCreatedBy())
                .updatedBy(expression.getUpdatedBy())
                .build();
    }
}
