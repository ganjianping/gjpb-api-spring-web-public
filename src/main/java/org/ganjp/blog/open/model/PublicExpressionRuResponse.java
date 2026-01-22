package org.ganjp.blog.open.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ganjp.blog.rubi.model.entity.ExpressionRu;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicExpressionRuResponse {
    private String id;
    private String name;
    private String phonetic;
    private String phoneticAudioUrl;
    private String translation;
    private String explanation;
    private String example;
    private Integer term;
    private Integer week;
    private String tags;
    private String difficultyLevel;
    private ExpressionRu.Language lang;
    private Integer displayOrder;
    private String updatedAt;
}
