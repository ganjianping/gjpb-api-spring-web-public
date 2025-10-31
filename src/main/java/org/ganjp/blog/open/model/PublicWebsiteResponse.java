package org.ganjp.blog.open.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** PublicWebsiteResponse holds public-facing website data. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicWebsiteResponse {
    private String id;
    private String name;
    private String description;
    private String url;
    private String logoUrl;
    private String tags;
    private String updatedAt;
}
