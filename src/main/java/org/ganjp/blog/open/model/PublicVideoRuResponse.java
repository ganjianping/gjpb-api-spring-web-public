package org.ganjp.blog.open.model;

import lombok.Builder;
import lombok.Data;
import org.ganjp.blog.rubi.model.entity.VideoRu;

@Data
@Builder
public class PublicVideoRuResponse {
    private String id;
    private String name;
    private String filename;
    private String fileUrl;
    private Long sizeBytes;
    private String coverImageFilename;
    private String coverImageFileUrl;
    private String originalUrl;
    private String sourceName;
    private String description;
    private Integer term;
    private Integer week;
    private String tags;
    private VideoRu.Language lang;
    private Integer displayOrder;
    private String updatedAt;
}
