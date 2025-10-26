package org.ganjp.blog.cms.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ganjp.blog.cms.model.entity.Article;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleUpdateRequest {
    private String title;
    private String summary;
    private String content;
    private String originalUrl;
    private String sourceName;
    private String coverImageFilename;
    private String coverImageOriginalUrl;
    private MultipartFile coverImageFile;
    private String tags;
    private Article.Language lang;
    private Integer displayOrder;
    private Boolean isActive;
}
