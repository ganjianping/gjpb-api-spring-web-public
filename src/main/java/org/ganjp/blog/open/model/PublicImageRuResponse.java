package org.ganjp.blog.open.model;

import lombok.Builder;
import lombok.Data;
import org.ganjp.blog.rubi.model.entity.ImageRu;

@Data
@Builder
public class PublicImageRuResponse {
    private String id;
    private String name;
    private String originalUrl;
    private String sourceName;
    private String filename;
    private String fileUrl;
    private String thumbnailFilename;
    private String thumbnailFileUrl;
    private String extension;
    private String mimeType;
    private Long sizeBytes;
    private Integer width;
    private Integer height;
    private String altText;
    private Integer term;
    private Integer week;
    private String tags;
    private ImageRu.Language lang;
    private Integer displayOrder;
    private String updatedAt;
}
