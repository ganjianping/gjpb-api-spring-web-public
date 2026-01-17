package org.ganjp.blog.open.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ganjp.blog.rubi.model.entity.FillBlankQuestionRu;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicFillBlankQuestionRuResponse {
    private String id;
    private String question;
    private String answer;
    private String explanation;
    private String difficultyLevel;
    private Integer failCount;
    private Integer successCount;
    private Integer term;
    private Integer week;
    private String tags;
    private FillBlankQuestionRu.Language lang;
    private Integer displayOrder;
    private String updatedAt;
}
