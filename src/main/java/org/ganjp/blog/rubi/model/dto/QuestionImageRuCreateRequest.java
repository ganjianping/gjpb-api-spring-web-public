package org.ganjp.blog.rubi.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ganjp.blog.rubi.model.entity.QuestionImageRu.Language;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionImageRuCreateRequest {
    @Size(max = 36, message = "Multiple Choice Question ID must not exceed 36 characters")
    private String multipleChoiceQuestionId;

    @Size(max = 36, message = "Free Text Question ID must not exceed 36 characters")
    private String freeTextQuestionId;

    @Pattern(regexp = "^https?://.*", message = "Original URL must be a valid HTTP/HTTPS URL")
    @Size(max = 500, message = "Original URL must not exceed 500 characters")
    private String originalUrl;

    @NotBlank(message = "Filename is required")
    @Size(max = 255, message = "Filename must not exceed 255 characters")
    private String filename;

    private MultipartFile file;

    @Builder.Default
    private Language lang = Language.EN;

    @Builder.Default
    private Integer displayOrder = 0;

    @Builder.Default
    private Boolean isActive = true;

    public boolean hasImageSource() {
        return (file != null && !file.isEmpty()) || (originalUrl != null && !originalUrl.trim().isEmpty());
    }

    public boolean hasQuestionReference() {
        return (multipleChoiceQuestionId != null && !multipleChoiceQuestionId.trim().isEmpty()) || (freeTextQuestionId != null && !freeTextQuestionId.trim().isEmpty());
    }
}
