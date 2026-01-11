package org.ganjp.blog.rubi.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ganjp.blog.rubi.model.entity.FillBlankQuestionRu;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateFillBlankQuestionRuRequest {

    private String question;
    private String answer;
    private String explanation;
    private String difficultyLevel;
    private String tags;
    private FillBlankQuestionRu.Language lang;
    private Integer displayOrder;
    private Boolean isActive;
}
