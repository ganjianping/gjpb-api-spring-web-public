package org.ganjp.blog.rubi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "rubi")
public class RubiProperties {
    private VocabularyConfig vocabulary = new VocabularyConfig();
    private ExpressionConfig expression = new ExpressionConfig();
    private SentenceConfig sentence = new SentenceConfig();
    private QuestionImageConfig questionImage = new QuestionImageConfig();

    @Data
    public static class VocabularyConfig {
        private String baseUrl;
        private ImageConfig image = new ImageConfig();
        private AudioConfig audio = new AudioConfig();
    }

    @Data
    public static class ExpressionConfig {
        private String baseUrl;
        private AudioConfig audio = new AudioConfig();
    }

    @Data
    public static class SentenceConfig {
        private String baseUrl;
        private AudioConfig audio = new AudioConfig();
    }

    @Data
    public static class QuestionImageConfig {
        private String baseUrl;
        private UploadConfig upload = new UploadConfig();
    }

    @Data
    public static class UploadConfig {
        private String directory;
        private Long maxFileSize;
        private ResizeConfig resize = new ResizeConfig();
    }

    @Data
    public static class ResizeConfig {
        private Integer maxSize;
    }

    @Data
    public static class ImageConfig {
        private String directory;
        private Long maxSize;
    }

    @Data
    public static class AudioConfig {
        private String directory;
        private Long maxSize;
    }
}
