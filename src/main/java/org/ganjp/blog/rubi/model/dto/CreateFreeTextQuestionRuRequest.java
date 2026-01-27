package org.ganjp.blog.rubi.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ganjp.blog.rubi.model.entity.FreeTextQuestionRu;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateFreeTextQuestionRuRequest {

    private String question;
    private String answer;

    private String description;
    private String questiona;
    private String answera;
    private String questionb;
    private String answerb;
    private String questionc;
    private String answerc;
    private String questiond;
    private String answerd;
    private String questione;
    private String answere;
    private String questionf;
    private String answerf;
    private String explanation;
    private String difficultyLevel;
    private Integer term;
    private Integer week;
    private String tags;

    @NotNull(message = "Language is required")
    private FreeTextQuestionRu.Language lang;

    private Integer displayOrder;
    private Boolean isActive;
}