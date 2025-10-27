package org.ganjp.blog.cms.model.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class CmsFileUpdateRequest {
    private String name;
    private String originalUrl;
    private String sourceName;
    private MultipartFile file;
    private String filename; // optional desired filename
    private String tags;
    private org.ganjp.blog.cms.model.entity.CmsFile.Language lang;
    private Integer displayOrder;
    private Boolean isActive;
}
