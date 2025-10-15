package org.ganjp.blog.open.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ganjp.blog.common.model.ApiResponse;
import org.ganjp.blog.open.model.OpenAppSettingDto;
import org.ganjp.blog.open.service.OpenService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
