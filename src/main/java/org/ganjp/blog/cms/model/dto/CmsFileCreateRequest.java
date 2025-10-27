package org.ganjp.blog.cms.model.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotBlank;

@Data
public class CmsFileCreateRequest {
    @NotBlank
    private String name;

    private String originalUrl;
    private String sourceName;
    private MultipartFile file;
    private String filename; // optional desired filename
    private String tags;
    private org.ganjp.blog.cms.model.entity.CmsFile.Language lang;
    private Integer displayOrder;
}
