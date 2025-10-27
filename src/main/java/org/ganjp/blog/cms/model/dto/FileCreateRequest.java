package org.ganjp.blog.cms.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ganjp.blog.cms.model.entity.File;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileCreateRequest {
    @NotBlank
    private String name;

    private String originalUrl;
    private String sourceName;
    private MultipartFile file;
    private String filename; // optional desired filename
    private String tags;
    private File.Language lang;
    private Integer displayOrder;
    private Boolean isActive;
}
