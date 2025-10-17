package org.ganjp.blog.cms.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoCreateRequest {
    private String name;
    private String originalUrl;
    private String sourceName;
    private MultipartFile file;
    private String coverImageUrl;
    private Integer width;
    private Integer height;
    private Integer duration;
    private String description;
    private String tags;
    private org.ganjp.blog.cms.model.entity.Video.Language lang;
    private Integer displayOrder;
    private Boolean isActive;
}
