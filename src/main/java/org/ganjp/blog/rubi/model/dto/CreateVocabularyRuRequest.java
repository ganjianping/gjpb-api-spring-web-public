package org.ganjp.blog.rubi.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ganjp.blog.rubi.model.entity.VocabularyRu;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateVocabularyRuRequest {

    @NotBlank(message = "Word is required")
    private String word;

    private String wordImageFilename;
    private String wordImageOriginalUrl;
    private MultipartFile wordImageFile;

    private String simplePastTense;
    private String pastPerfectTense;
    private String translation;
    private String synonyms;
    private String pluralForm;
    private String phonetic;

    private String phoneticAudioFilename;
    private String phoneticAudioOriginalUrl;
    private MultipartFile phoneticAudioFile;

    private String partOfSpeech;

    private String definition;

    private String example;
    private String dictionaryUrl;
    private String tags;
    private String difficultyLevel;

    @NotNull(message = "Language is required")
    private VocabularyRu.Language lang;

    private Integer displayOrder;
    private Boolean isActive;
}
