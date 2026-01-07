package org.ganjp.blog.rubi.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ganjp.blog.rubi.model.entity.QuestionImageRu.Language;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionImageRuResponse {
    private String id;
    private String mcqId;
    private String saqId;
    private String filename;
    private String fileUrl;
    private String originalUrl;
    private Integer width;
    private Integer height;
    private Language lang;
    private Integer displayOrder;
    private String createdBy;
    private String updatedBy;
    private Boolean isActive;
    private String createdAt;
    private String updatedAt;
}
