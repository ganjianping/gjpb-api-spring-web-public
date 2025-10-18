package org.ganjp.blog.open.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ganjp.blog.common.model.ApiResponse;
import org.ganjp.blog.open.model.OpenAppSettingDto;
import org.ganjp.blog.open.service.OpenService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpRange;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Open REST Controller for accessing data without authentication
 * These endpoints are publicly accessible and don't require JWT tokens
 */
@RestController
@RequestMapping("/v1/open")
@RequiredArgsConstructor
@Slf4j
public class OpenController {

    private final OpenService openService;

    /**
     * Get all public app settings (only name, value, lang)
     * No authentication required
     * Only returns settings marked as public
     */
    @GetMapping("/app-settings")
    public ResponseEntity<ApiResponse<List<OpenAppSettingDto>>> getAllAppSettings() {
        List<OpenAppSettingDto> settings = openService.getAllAppSettings();
        return ResponseEntity.ok(ApiResponse.success(settings, "Public app settings retrieved successfully"));
    }

    /**
     * View logo image by filename
     * GET /v1/open/logos/{filename}
     * No authentication required
     * Returns the actual image file to be displayed in browser
     */
    @GetMapping("/logos/{filename}")
    public ResponseEntity<Resource> viewLogo(@PathVariable String filename) {
        try {
            File logoFile = openService.getLogoFile(filename);
            Resource resource = new FileSystemResource(logoFile);
            
            // Determine content type based on file extension
            String contentType = determineContentType(filename);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(resource);
        } catch (IllegalArgumentException e) {
            log.error("Logo not found: {}", filename, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IOException e) {
            log.error("Error reading logo file: {}", filename, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * View image by filename
     * GET /v1/open/images/{filename}
     * No authentication required
     * Returns the actual image file to be displayed in browser
     */
    @GetMapping("/images/{filename}")
    public ResponseEntity<Resource> viewImage(@PathVariable String filename) {
        try {
            File imageFile = openService.getImageFile(filename);
            Resource resource = new FileSystemResource(imageFile);
            // Determine content type based on file extension
            String contentType = org.ganjp.blog.cms.util.CmsUtil.determineContentType(filename);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(resource);
        } catch (IllegalArgumentException e) {
            log.error("Image not found: {}", filename, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IOException e) {
            log.error("Error reading image file: {}", filename, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/videos/cover-image/{filename}")
    public ResponseEntity<Resource> viewVideoCoverImage(@PathVariable String filename) {
        try {
            File imageFile = openService.getVideoCoverFile(filename);
            if (imageFile == null || !imageFile.exists()) {
                log.error("Video cover image not found: {}", filename);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            Resource resource = new FileSystemResource(imageFile);
            // Determine content type based on file extension
            String contentType = org.ganjp.blog.cms.util.CmsUtil.determineContentType(filename);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(resource);
        } catch (IllegalArgumentException e) {
            log.error("Video cover image not found: {}", filename, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IOException e) {
            log.error("Error reading video cover image: {}", filename, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * View video by filename
     * GET /v1/open/videos/{filename}
     * No authentication required
     * Returns video file for download/streaming. Range requests are not handled here — the video file is returned as-is.
     */
    @GetMapping("/videos/{filename}")
    public ResponseEntity<?> viewVideo(@PathVariable String filename, @RequestHeader(value = "Range", required = false) String rangeHeader) {
        try {
            java.io.File file = openService.getVideoFile(filename);
            long contentLength = file.length();
            String contentType = org.ganjp.blog.cms.util.CmsUtil.determineContentType(filename);

            if (rangeHeader == null) {
                Resource resource = new FileSystemResource(file);
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                        .contentLength(contentLength)
                        .body(resource);
            }

            HttpRange httpRange = HttpRange.parseRanges(rangeHeader).get(0);
            long start = httpRange.getRangeStart(contentLength);
            long end = httpRange.getRangeEnd(contentLength);
            long rangeLength = end - start + 1;

            java.io.InputStream rangeStream = new java.io.InputStream() {
                private final java.io.RandomAccessFile raf;
                private long remaining = rangeLength;
                {
                    this.raf = new java.io.RandomAccessFile(file, "r");
                    this.raf.seek(start);
                }
                @Override
                public int read() throws java.io.IOException {
                    if (remaining <= 0) return -1;
                    int b = raf.read();
                    if (b != -1) remaining--;
                    return b;
                }
                @Override
                public int read(byte[] b, int off, int len) throws java.io.IOException {
                    if (remaining <= 0) return -1;
                    int toRead = (int) Math.min(len, remaining);
                    int r = raf.read(b, off, toRead);
                    if (r > 0) remaining -= r;
                    return r;
                }
                @Override
                public void close() throws java.io.IOException {
                    try { raf.close(); } finally { super.close(); }
                }
            };

            InputStreamResource resource = new InputStreamResource(rangeStream);

            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + contentLength)
                    .contentLength(rangeLength)
                    .body(resource);

        } catch (IllegalArgumentException e) {
            log.error("Video not found: {}", filename, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IOException e) {
            log.error("Error reading video file: {}", filename, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Determine content type based on file extension
     */
    private String determineContentType(String filename) {
        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        return switch (extension) {
            case "png" -> "image/png";
            case "jpg", "jpeg" -> "image/jpeg";
            case "gif" -> "image/gif";
            case "svg" -> "image/svg+xml";
            case "webp" -> "image/webp";
            case "bmp" -> "image/bmp";
            default -> "application/octet-stream";
        };
    }
}
