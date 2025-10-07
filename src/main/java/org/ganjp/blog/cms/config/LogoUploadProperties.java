package org.ganjp.blog.cms.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for logo upload
 */
@Configuration
@ConfigurationProperties(prefix = "logo.upload")
@Data
public class LogoUploadProperties {

    /**
     * Directory where logos are stored
     */
    private String directory = "uploads/logos";

    /**
     * Maximum file size in bytes (default 5MB)
     */
    private long maxFileSize = 5242880;

    /**
     * Base URL path for accessing uploaded logos
     * Can be relative path (e.g., /uploads/logos) or absolute URL (e.g., https://cdn.example.com/logos)
     */
    private String baseUrl = "/uploads/logos";

    /**
     * Resize configuration
     */
    private Resize resize = new Resize();

    @Data
    public static class Resize {
        /**
         * Target size for width or height (default 256px)
         */
        private int targetSize = 256;
    }
}
