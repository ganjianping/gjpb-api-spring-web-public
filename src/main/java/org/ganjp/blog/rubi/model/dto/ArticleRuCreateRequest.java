package org.ganjp.blog.rubi.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ganjp.blog.rubi.model.entity.ArticleRu;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleRuCreateRequest {
    private String title;
    private String summary;
    private String content;
    private String originalUrl;
    private String sourceName;
    private String coverImageFilename;
    private String coverImageOriginalUrl;
    private MultipartFile coverImageFile;
    private Integer term;
    private Integer week;
    private String tags;
    private ArticleRu.Language lang;
    private Integer displayOrder;
    private Boolean isActive;
}
