package org.ganjp.blog.rubi.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ganjp.blog.rubi.model.entity.ImageRu.Language;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageRuResponse {
    private String id;
    private String name;
    private String originalUrl;
    private String sourceName;
    private String filename;
    private String thumbnailFilename;
    private String extension;
    private String mimeType;
    private Long sizeBytes;
    private Integer width;
    private Integer height;
    private String altText;
    private String tags;
    private Language lang;
    private Integer displayOrder;
    private String createdBy;
    private String updatedBy;
    private Boolean isActive;
    private String createdAt;
    private String updatedAt;
}
