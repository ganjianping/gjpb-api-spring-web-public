package org.ganjp.blog.rubi.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ganjp.blog.rubi.model.entity.SentenceRu;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSentenceRuRequest {

    private String name;
    private String phonetic;
    private String translation;
    private String explanation;
    private Integer term;
    private Integer week;
    private String tags;
    private String difficultyLevel;
    private SentenceRu.Language lang;
    private Integer displayOrder;
    private Boolean isActive;
}
