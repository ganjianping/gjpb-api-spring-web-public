package org.ganjp.blog.rubi.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ganjp.blog.rubi.model.entity.SentenceRu;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SentenceRuResponse {

    private String id;
    private String name;
    private String phonetic;
    private String translation;
    private String explanation;
    private String tags;
    private String difficultyLevel;
    private SentenceRu.Language lang;
    private Integer displayOrder;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    public static SentenceRuResponse fromEntity(SentenceRu sentence) {
        if (sentence == null) {
            return null;
        }
        return SentenceRuResponse.builder()
                .id(sentence.getId())
                .name(sentence.getName())
                .phonetic(sentence.getPhonetic())
                .translation(sentence.getTranslation())
                .explanation(sentence.getExplanation())
                .tags(sentence.getTags())
                .difficultyLevel(sentence.getDifficultyLevel())
                .lang(sentence.getLang())
                .displayOrder(sentence.getDisplayOrder())
                .isActive(sentence.getIsActive())
                .createdAt(sentence.getCreatedAt())
                .updatedAt(sentence.getUpdatedAt())
                .createdBy(sentence.getCreatedBy())
                .updatedBy(sentence.getUpdatedBy())
                .build();
    }
}
