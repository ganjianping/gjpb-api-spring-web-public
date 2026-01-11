package org.ganjp.blog.rubi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "rubi.audio")
@Data
public class AudioRuProperties {
    private String baseUrl;
    private Upload upload;

    @Data
    public static class Upload {
        private String directory;
        private Long maxFileSize;
        private CoverImage coverImage;
    }

    @Data
    public static class CoverImage {
        private Integer maxSize;
    }
}
