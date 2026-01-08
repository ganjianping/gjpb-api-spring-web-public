package org.ganjp.blog.rubi.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ganjp.blog.rubi.model.entity.MultipleChoiceQuestionRu;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMultipleChoiceQuestionRuRequest {

    private String question;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String correctAnswers;
    private String explanation;
    private String difficultyLevel;
    private String tags;
    private MultipleChoiceQuestionRu.Language lang;
    private Integer displayOrder;
    private Boolean isActive;
}