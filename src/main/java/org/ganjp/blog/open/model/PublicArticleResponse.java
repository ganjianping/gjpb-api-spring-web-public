package org.ganjp.blog.open.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ganjp.blog.cms.model.entity.Article;

/**
 * PublicArticleResponse represents the public-facing article items used in
 * list endpoints (paginated). It includes only fields intended for public
 * consumption and includes a computed coverImageUrl.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicArticleResponse {
    private String id;
    private String title;
    private String summary;
    private String originalUrl;
    private String sourceName;
    private String coverImageOriginalUrl;
    private String coverImageUrl;
    private String tags;
    private Article.Language lang;
    private Integer displayOrder;
    private String updatedAt;
}
