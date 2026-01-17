package org.ganjp.blog.rubi.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ganjp.blog.rubi.model.entity.TrueFalseQuestionRu;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrueFalseQuestionRuResponse {

    private String id;
    private String question;
    private TrueFalseQuestionRu.Answer answer;
    private String explanation;
    private String difficultyLevel;
    private Integer failCount;
    private Integer successCount;
    private Integer term;
    private Integer week;
    private String tags;
    private TrueFalseQuestionRu.Language lang;
    private Integer displayOrder;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
