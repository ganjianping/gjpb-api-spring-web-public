package org.ganjp.blog.open.model;

import lombok.Builder;
import lombok.Data;
import org.ganjp.blog.rubi.model.entity.ArticleRu;

@Data
@Builder
public class PublicArticleRuResponse {
    private String id;
    private String title;
    private String summary;
    private String content;
    private String originalUrl;
    private String sourceName;
    private String coverImageFilename;
    private String coverImageFileUrl;
    private String coverImageOriginalUrl;
    private Integer term;
    private Integer week;
    private String tags;
    private ArticleRu.Language lang;
    private Integer displayOrder;
    private String updatedAt;
}
