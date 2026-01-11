package org.ganjp.blog.rubi.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ganjp.blog.rubi.model.entity.FillBlankQuestionRu;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FillBlankQuestionRuResponse {

    private String id;
    private String question;
    private String answer;
    private String explanation;
    private String difficultyLevel;
    private Integer failCount;
    private Integer successCount;
    private String tags;
    private FillBlankQuestionRu.Language lang;
    private Integer displayOrder;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
