package org.ganjp.blog.rubi.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoRuUpdateRequest {
    private String name;
    private String filename; // optional if external
    private String originalUrl;
    private String sourceName;
    private String coverImageFilename;
    // allow uploading a new cover image when updating
    private MultipartFile coverImageFile;
    private String description;
    private String tags;

    private org.ganjp.blog.rubi.model.entity.VideoRu.Language lang;
    private Integer displayOrder;
    private Boolean isActive;
}
