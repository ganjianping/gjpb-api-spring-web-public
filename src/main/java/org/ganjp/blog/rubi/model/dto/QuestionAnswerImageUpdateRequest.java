package org.ganjp.blog.rubi.model.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ganjp.blog.rubi.model.entity.QuestionAnswerImage.Language;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionAnswerImageUpdateRequest {
    @Size(max = 36)
    private String mcqId;

    @Size(max = 36)
    private String saqId;

    @Size(max = 500)
    private String originalUrl;

    private Language lang;

    private Integer displayOrder;

    private Boolean isActive;
}
