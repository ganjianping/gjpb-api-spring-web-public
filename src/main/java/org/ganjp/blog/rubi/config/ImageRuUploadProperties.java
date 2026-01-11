package org.ganjp.blog.rubi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "rubi.image.upload")
public class ImageRuUploadProperties {
    private String directory;
    private String baseUrl;
    private Long maxFileSize;
    private Resize resize;

    public static class Resize {
        private Integer maxSize;
        private Integer thumbnailSize;
        public Integer getMaxSize() { return maxSize; }
        public void setMaxSize(Integer maxSize) { this.maxSize = maxSize; }
        public Integer getThumbnailSize() { return thumbnailSize; }
        public void setThumbnailSize(Integer thumbnailSize) { this.thumbnailSize = thumbnailSize; }
    }

    public String getDirectory() { return directory; }
    public void setDirectory(String directory) { this.directory = directory; }
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public Long getMaxFileSize() { return maxFileSize; }
    public void setMaxFileSize(Long maxFileSize) { this.maxFileSize = maxFileSize; }
    public Resize getResize() { return resize; }
    public void setResize(Resize resize) { this.resize = resize; }
}
