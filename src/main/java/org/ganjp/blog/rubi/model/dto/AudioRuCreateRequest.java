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
public class AudioRuCreateRequest {
    private String name;
    private String filename;
    private MultipartFile file;
    private String originalUrl;
    private String sourceName;
    private String coverImageFilename;
    private MultipartFile coverImageFile;
    private String description;
    private String subtitle;
    private String artist;
    private Integer term;
    private Integer week;
    private String tags;
    private org.ganjp.blog.rubi.model.entity.AudioRu.Language lang;
    private Integer displayOrder;
    private Boolean isActive;
}
