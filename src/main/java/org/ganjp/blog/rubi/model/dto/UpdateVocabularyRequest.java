package org.ganjp.blog.rubi.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ganjp.blog.rubi.model.entity.Vocabulary;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateVocabularyRequest {

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
    private Vocabulary.Language lang;
    private Integer displayOrder;
    private Boolean isActive;
}
