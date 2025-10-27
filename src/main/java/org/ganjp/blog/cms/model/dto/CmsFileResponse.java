package org.ganjp.blog.cms.model.dto;

import lombok.Data;

@Data
public class CmsFileResponse {
    private String id;
    private String name;
    private String originalUrl;
    private String sourceName;
    private String filename;
    private Long sizeBytes;
    private String extension;
    private String mimeType;
    private String tags;
    private org.ganjp.blog.cms.model.entity.CmsFile.Language lang;
    private Integer displayOrder;
    private String createdAt;
    private String updatedAt;
    private String createdBy;
    private String updatedBy;
    private Boolean isActive;
}
