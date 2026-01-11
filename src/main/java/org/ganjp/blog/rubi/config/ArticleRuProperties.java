package org.ganjp.blog.rubi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "rubi.article")
@Data
public class ArticleRuProperties {
    private CoverImage coverImage;
    private ContentImage contentImage;

    @Data
    public static class CoverImage {
        private String baseUrl;
        private Upload upload;
    }

    @Data
    public static class ContentImage {
        private String baseUrl;
        private Upload upload;
    }

    @Data
    public static class Upload {
        private String directory;
        private Long maxFileSize;
        private Resize resize;
    }

    @Data
    public static class Resize {
        private Integer maxSize;
    }
}
