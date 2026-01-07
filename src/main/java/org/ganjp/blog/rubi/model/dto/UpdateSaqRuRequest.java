package org.ganjp.blog.rubi.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ganjp.blog.rubi.model.entity.SaqRu;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSaqRuRequest {

    private String question;
    private String correctAnswer;
    private String explanation;
    private String difficultyLevel;
    private String tags;
    private SaqRu.Language lang;
    private Integer displayOrder;
    private Boolean isActive;
}