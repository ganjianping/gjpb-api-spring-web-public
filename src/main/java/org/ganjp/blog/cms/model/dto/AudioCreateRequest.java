package org.ganjp.blog.cms.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AudioCreateRequest {
    private String name;
    private MultipartFile file;
    private String originalUrl;
    private String sourceName;
    private String coverImageFilename;
    private MultipartFile coverImageFile;
    private String description;
    private String subtitle;
    private String tags;
    private org.ganjp.blog.cms.model.entity.Video.Language lang;
    private Integer displayOrder;
    private Boolean isActive;
}
