package org.ganjp.blog.rubi.model.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ganjp.blog.rubi.model.entity.ArticleImageRu.Language;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleImageRuUpdateRequest {
    @Size(max = 36)
    private String articleRuId;

    @Size(max = 500)
    private String articleRuTitle;

    @Size(max = 500)
    private String originalUrl;

    private Language lang;

    private Integer displayOrder;

    private Boolean isActive;
}
