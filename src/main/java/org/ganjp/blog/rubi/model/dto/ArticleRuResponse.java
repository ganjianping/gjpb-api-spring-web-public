package org.ganjp.blog.rubi.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ganjp.blog.rubi.model.entity.ArticleRu;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleRuResponse {
    private String id;
    private String title;
    private String summary;
    private String content;
    private String originalUrl;
    private String sourceName;
    private String coverImageFilename;
    private String coverImageFileUrl;
    private String coverImageOriginalUrl;
    private String tags;
    private ArticleRu.Language lang;
    private Integer displayOrder;
    private String createdBy;
    private String updatedBy;
    private Boolean isActive;
    private String createdAt;
    private String updatedAt;
}
