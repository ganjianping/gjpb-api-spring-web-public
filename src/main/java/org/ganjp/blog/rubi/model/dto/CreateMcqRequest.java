package org.ganjp.blog.rubi.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ganjp.blog.rubi.model.entity.Mcq;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMcqRequest {

    @NotBlank(message = "Question is required")
    private String question;

    @NotBlank(message = "Option A is required")
    private String optionA;

    @NotBlank(message = "Option B is required")
    private String optionB;

    @NotBlank(message = "Option C is required")
    private String optionC;

    @NotBlank(message = "Option D is required")
    private String optionD;

    @NotBlank(message = "Correct answers are required")
    private String correctAnswers;

    private Boolean isMultipleCorrect;
    private String explanation;
    private String difficultyLevel;
    private String tags;

    @NotNull(message = "Language is required")
    private Mcq.Language lang;

    private Integer displayOrder;
    private Boolean isActive;
}