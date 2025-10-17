package org.ganjp.blog.cms.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoUpdateRequest {
    private String name;
    private String originalUrl;
    private String sourceName;
    private String filename; // optional if external
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
