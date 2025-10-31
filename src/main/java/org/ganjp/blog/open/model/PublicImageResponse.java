package org.ganjp.blog.open.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ganjp.blog.cms.model.entity.Image;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicImageResponse {
    private String id;
    private String name;
    private String description;
    private String url;
    private String thumbnailUrl;
    private String originalUrl;
    private String tags;
    private Image.Language lang;
    private Integer displayOrder;
    private String updatedAt;
}
