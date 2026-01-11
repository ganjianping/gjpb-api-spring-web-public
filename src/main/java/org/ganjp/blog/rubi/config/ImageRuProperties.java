package org.ganjp.blog.rubi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "rubi.image")
@Data
public class ImageRuProperties {
    private String baseUrl;
    private Upload upload;

    @Data
    public static class Upload {
        private String directory;
        private Long maxFileSize;
        private Resize resize;
    }

    @Data
    public static class Resize {
        private Integer maxSize;
        private Integer thumbnailSize;
    }
}
