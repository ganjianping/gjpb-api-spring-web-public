package org.ganjp.blog.open.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ganjp.blog.rubi.model.entity.MultipleChoiceQuestionRu;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicMultipleChoiceQuestionRuResponse {
    private String id;
    private String question;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String answer;
    private String explanation;
    private String difficultyLevel;
    private Integer failCount;
    private Integer successCount;
    private Integer term;
    private Integer week;
    private String tags;
    private MultipleChoiceQuestionRu.Language lang;
    private Integer displayOrder;
    private String updatedAt;
}
