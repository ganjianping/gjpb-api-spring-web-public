package org.ganjp.blog.cms.controller;

import lombok.RequiredArgsConstructor;
import org.ganjp.blog.auth.security.JwtUtils;
import org.ganjp.blog.cms.model.dto.*;
import org.ganjp.blog.cms.service.ImageService;
import org.ganjp.blog.cms.util.CmsUtil;
import org.ganjp.blog.common.model.ApiResponse;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/v1/images")
@RequiredArgsConstructor
public class ImageController {
    private final ImageService imageService;
    private final JwtUtils jwtUtils;

    /**
     * Create a new image from file upload
     * POST /v1/images/upload
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ImageResponse>> createImage(
            @Valid @ModelAttribute ImageCreateRequest request,
            HttpServletRequest httpRequest) {
        try {
            String userId = extractUserIdFromRequest(httpRequest);
            ImageResponse response = imageService.createImage(request, userId);
            return ResponseEntity.status(201).body(ApiResponse.success(response, "Image created successfully"));
        } catch (IOException e) {
            log.error("Error creating image", e);
            return ResponseEntity.status(500).body(ApiResponse.error(500, "Error creating image: " + e.getMessage(), null));
        } catch (IllegalArgumentException e) {
            log.error("Invalid request", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "Invalid request: " + e.getMessage(), null));
        }
    }

    /**
     * Create a new image from URL (JSON payload)
     * POST /v1/images (application/json)
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<ImageResponse>> createImageFromUrl(
            @Valid @RequestBody ImageCreateRequest request,
            HttpServletRequest httpRequest) {
        try {
            if (request.getOriginalUrl() == null || request.getOriginalUrl().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error(400, "originalUrl is required", null));
            }
            String userId = extractUserIdFromRequest(httpRequest);
            ImageResponse response = imageService.createImage(request, userId);
            return ResponseEntity.status(201).body(ApiResponse.success(response, "Image created successfully from URL"));
        } catch (IOException e) {
            log.error("Error creating image from URL", e);
            return ResponseEntity.status(500).body(ApiResponse.error(500, "Error creating image from URL: " + e.getMessage(), null));
        } catch (IllegalArgumentException e) {
            log.error("Invalid URL or request", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "Invalid request: " + e.getMessage(), null));
        }
    }
        /**
     * Extract user ID from JWT token in the Authorization header
     * @param request HttpServletRequest containing the Authorization header
     * @return User ID extracted from token
     */
    private String extractUserIdFromRequest(HttpServletRequest request) {
        return jwtUtils.extractUserIdFromToken(request);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ImageResponse>> getImageById(@PathVariable String id) {
        try {
            ImageResponse response = imageService.getImageById(id);
            if (response == null) {
                return ResponseEntity.status(404).body(ApiResponse.error(404, "Image not found", null));
            }
            return ResponseEntity.ok(ApiResponse.success(response, "Image found"));
        } catch (Exception e) {
            log.error("Error fetching image", e);
            return ResponseEntity.status(500).body(ApiResponse.error(500, "Error fetching image: " + e.getMessage(), null));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ImageResponse>>> listImages() {
        try {
            List<ImageResponse> images = imageService.listImages();
            return ResponseEntity.ok(ApiResponse.success(images, "Images listed"));
        } catch (Exception e) {
            log.error("Error listing images", e);
            return ResponseEntity.status(500).body(ApiResponse.error(500, "Error listing images: " + e.getMessage(), null));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ImageResponse>> updateImage(
            @PathVariable String id,
            @Valid @RequestBody ImageUpdateRequest request,
            HttpServletRequest httpRequest) {
        try {
            String userId = extractUserIdFromRequest(httpRequest);
            ImageResponse response = imageService.updateImage(id, request, userId);
            if (response == null) {
                return ResponseEntity.status(404).body(ApiResponse.error(404, "Image not found", null));
            }
            return ResponseEntity.ok(ApiResponse.success(response, "Image updated"));
        } catch (Exception e) {
            log.error("Error updating image", e);
            return ResponseEntity.status(500).body(ApiResponse.error(500, "Error updating image: " + e.getMessage(), null));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteImage(
            @PathVariable String id,
            HttpServletRequest httpRequest) {
        try {
            String userId = extractUserIdFromRequest(httpRequest);
            boolean deleted = imageService.deleteImage(id, userId);
            if (!deleted) {
                return ResponseEntity.status(404).body(ApiResponse.error(404, "Image not found", null));
            }
            return ResponseEntity.ok(ApiResponse.success(null, "Image deleted"));
        } catch (Exception e) {
            log.error("Error deleting image", e);
            return ResponseEntity.status(500).body(ApiResponse.error(500, "Error deleting image: " + e.getMessage(), null));
        }
    }

    /**
     * View image file in browser by filename
     * GET /v1/images/view/{filename}
     * Returns the actual image file to be displayed in browser
     */
    @GetMapping("/view/{filename}")
    public ResponseEntity<Resource> viewImage(@PathVariable String filename) {
        try {
            File imageFile = imageService.getImageFileByFilename(filename);
            Resource resource = new FileSystemResource(imageFile);

            // Determine content type based on file extension
            String contentType = CmsUtil.determineContentType(filename);

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

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ImageResponse>>> searchImages(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) org.ganjp.blog.cms.model.entity.Image.Language lang,
            @RequestParam(required = false) String tags,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) String keyword
    ) {
        try {
            // backward compatibility: if keyword is provided use the old simple search
            List<ImageResponse> images;
            if (keyword != null && !keyword.isBlank()) {
                images = imageService.searchImages(keyword);
            } else {
                images = imageService.searchImages(name, lang, tags, isActive);
            }
            return ResponseEntity.ok(ApiResponse.success(images, "Images found"));
        } catch (Exception e) {
            log.error("Error searching images", e);
            return ResponseEntity.status(500).body(ApiResponse.error(500, "Error searching images: " + e.getMessage(), null));
        }
    }
}
