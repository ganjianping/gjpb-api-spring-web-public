package org.ganjp.blog.rubi.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ganjp.blog.rubi.model.entity.AudioRu;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AudioRuResponse {
    private String id;
    private String name;
    private String filename;
    private String fileUrl;
    private Long sizeBytes;
    private String coverImageFilename;
    private String coverImageFileUrl;
    private String originalUrl;
    private String sourceName;
    private String subtitle;
    private String description;
    private String artist;
    private String tags;
    private AudioRu.Language lang;
    private Integer displayOrder;
    private String createdBy;
    private String updatedBy;
    private Boolean isActive;
    private String createdAt;
    private String updatedAt;
}
