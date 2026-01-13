package org.ganjp.blog.rubi.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ganjp.blog.rubi.model.entity.ExpressionRu;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateExpressionRuRequest {

    private String name;
    private String phonetic;
    private String translation;
    private String explanation;
    private String example;
    private String tags;
    private String difficultyLevel;
    private ExpressionRu.Language lang;
    private Integer displayOrder;
    private Boolean isActive;
}
