package org.ganjp.blog.open.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ganjp.blog.cms.model.entity.Logo;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicLogoResponse {
    private String id;
    private String name;
    private String url;
    private String thumbnailUrl;
    private String tags;
    private Logo.Language lang;
    private Integer displayOrder;
    private String updatedAt;
}
