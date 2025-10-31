package org.ganjp.blog.open.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ganjp.blog.cms.model.entity.Audio;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicAudioResponse {
    private String id;
    private String title;
    private String description;
    private String url;
    private String coverImageUrl;
    private String tags;
    private Audio.Language lang;
    private Integer displayOrder;
    private String updatedAt;
}
