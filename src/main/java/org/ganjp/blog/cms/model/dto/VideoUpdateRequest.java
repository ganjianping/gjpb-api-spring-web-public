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
public class VideoUpdateRequest {
    private String name;
    private String filename; // optional if external
    private String coverImageFilename;
    // allow uploading a new cover image when updating
    private MultipartFile coverImageFile;
    private String description;
    private String tags;

    private org.ganjp.blog.cms.model.entity.Video.Language lang;
    private Integer displayOrder;
    private Boolean isActive;
}
