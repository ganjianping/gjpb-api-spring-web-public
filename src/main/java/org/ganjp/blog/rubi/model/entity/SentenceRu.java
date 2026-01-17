package org.ganjp.blog.rubi.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.ganjp.blog.common.model.entity.BaseEntity;

/**
 * Sentence Entity for Rubi
 */
@Entity
@Table(name = "rubi_sentence")
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SentenceRu extends BaseEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "name", length = 400, nullable = false)
    private String name;

    @Column(name = "phonetic", length = 400)
    private String phonetic;

    @Column(name = "translation", length = 400)
    private String translation;

    @Column(name = "explanation", length = 1000)
    private String explanation;

    @Column(name = "term")
    private Integer term;

    @Column(name = "week")
    private Integer week;

    @Column(name = "tags", length = 500)
    private String tags;

    @Column(name = "difficulty_level", length = 20)
    private String difficultyLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "lang", nullable = false)
    @Builder.Default
    private Language lang = Language.EN;

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /**
     * Language enumeration
     */
    public enum Language {
        EN,
        ZH
    }
}
