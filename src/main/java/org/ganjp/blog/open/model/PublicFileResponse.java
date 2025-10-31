package org.ganjp.blog.open.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ganjp.blog.cms.model.entity.File;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicFileResponse {
    private String id;
    private String name;
    private String description;
    private String url;
    private String originalUrl;
    private String tags;
    private File.Language lang;
    private Integer displayOrder;
    private String updatedAt;
}
