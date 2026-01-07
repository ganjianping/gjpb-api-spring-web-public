package org.ganjp.blog.rubi.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ganjp.blog.rubi.model.entity.Saq;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSaqRequest {

    @NotBlank(message = "Question is required")
    private String question;

    @NotBlank(message = "Correct answer is required")
    private String correctAnswer;

    private String explanation;
    private String difficultyLevel;
    private String tags;

    @NotNull(message = "Language is required")
    private Saq.Language lang;

    private Integer displayOrder;
    private Boolean isActive;
}