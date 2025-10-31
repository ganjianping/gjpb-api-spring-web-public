package org.ganjp.blog.open.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ganjp.blog.cms.model.entity.Article;

/**
 * PublicArticleDetailResponse represents the public-facing article detail
 * returned by the public API. It mirrors a subset of the internal
 * ArticleResponse but only contains fields intended for public consumption.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicArticleDetailResponse {
    private String id;
    private String title;
    private String summary;
    private String content;
    private String originalUrl;
    private String sourceName;
    private String coverImageOriginalUrl;
    private String coverImageUrl;
    private String tags;
    private Article.Language lang;
    private Integer displayOrder;
    private String updatedAt;
}
