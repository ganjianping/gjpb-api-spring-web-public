package org.ganjp.blog.open.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ganjp.blog.rubi.model.entity.FreeTextQuestionRu;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicFreeTextQuestionRuResponse {
    private String id;
    private String question;
    private String answer;
    private String description;
    private String questiona;
    private String answera;
    private String questionb;
    private String answerb;
    private String questionc;
    private String answerc;
    private String questiond;
    private String answerd;
    private String questione;
    private String answere;
    private String questionf;
    private String answerf;
    private String explanation;
    private String difficultyLevel;
    private Integer failCount;
    private Integer successCount;
    private Integer term;
    private Integer week;
    private String tags;
    private FreeTextQuestionRu.Language lang;
    private Integer displayOrder;
    private String grammarChapter;
    private String scienceChapter;
    private String updatedAt;
}
