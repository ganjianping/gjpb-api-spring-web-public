package org.ganjp.blog.rubi.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ganjp.blog.rubi.model.entity.TrueFalseQuestionRu;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTrueFalseQuestionRuRequest {

    private String question;
    private TrueFalseQuestionRu.Answer answer;
    private String explanation;
    private String difficultyLevel;
    private String tags;
    private TrueFalseQuestionRu.Language lang;
    private Integer displayOrder;
    private Boolean isActive;
}
