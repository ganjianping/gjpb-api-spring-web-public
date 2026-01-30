package org.ganjp.blog.rubi.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ganjp.blog.rubi.model.entity.TrueFalseQuestionRu;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTrueFalseQuestionRuRequest {

    @NotBlank(message = "Question is required")
    private String question;

    @NotNull(message = "Answer is required")
    private TrueFalseQuestionRu.Answer answer;

    private String explanation;
    private String difficultyLevel;
    private Integer term;
    private Integer week;
    private String tags;

    @NotNull(message = "Language is required")
    private TrueFalseQuestionRu.Language lang;

    private Integer displayOrder;
    private Boolean isActive;
    private String grammarChapter;
    private String scienceChapter;
}
