package org.ganjp.blog.open.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ganjp.blog.rubi.model.entity.TrueFalseQuestionRu;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicTrueFalseQuestionRuResponse {
    private String id;
    private String question;
    private TrueFalseQuestionRu.Answer answer;
    private String explanation;
    private String difficultyLevel;
    private Integer failCount;
    private Integer successCount;
    private Integer term;
    private Integer week;
    private String tags;
    private TrueFalseQuestionRu.Language lang;
    private Integer displayOrder;
    private String updatedAt;
}
