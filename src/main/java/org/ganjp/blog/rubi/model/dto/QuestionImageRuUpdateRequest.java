package org.ganjp.blog.rubi.model.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ganjp.blog.rubi.model.entity.QuestionImageRu.Language;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionImageRuUpdateRequest {
    @Size(max = 36)
    private String multipleChoiceQuestionId;

    @Size(max = 36)
    private String freeTextQuestionId;

    @Size(max = 36)
    private String trueFalseQuestionId;

    @Size(max = 36)
    private String fillBlankQuestionId;

    @Size(max = 500)
    private String originalUrl;

    private Language lang;

    private Integer displayOrder;

    private Boolean isActive;
}
