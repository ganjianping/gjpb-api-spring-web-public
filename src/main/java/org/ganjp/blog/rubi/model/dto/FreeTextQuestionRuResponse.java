package org.ganjp.blog.rubi.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ganjp.blog.rubi.model.entity.FreeTextQuestionRu;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FreeTextQuestionRuResponse {

    private String id;
    private String question;
    private String answer;
    private String explanation;
    private String difficultyLevel;
    private Integer term;
    private Integer week;
    private String tags;
    private FreeTextQuestionRu.Language lang;
    private Integer displayOrder;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}