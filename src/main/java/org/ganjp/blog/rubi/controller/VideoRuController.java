package org.ganjp.blog.rubi.controller;

import lombok.RequiredArgsConstructor;
import org.ganjp.blog.auth.security.JwtUtils;
import org.ganjp.blog.rubi.model.dto.*;
import org.ganjp.blog.rubi.service.VideoRuService;
import org.ganjp.blog.common.model.ApiResponse;
import org.ganjp.blog.common.model.PaginatedResponse;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpRange;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/v1/video-rus")
@RequiredArgsConstructor
public class VideoRuController {
    private final VideoRuService videoRuService;
    private final JwtUtils jwtUtils;

    /**
     * Search videos with pagination and filtering
     * GET /v1/videos?name=xxx&lang=EN&tags=yyy&isActive=true&page=0&size=20&sort=updatedAt&direction=desc
     * 
     * @param page Page number (0-based)
     * @param size Page size
     * @param sort Sort field (e.g., updatedAt, createdAt, name)
     * @param direction Sort direction (asc or desc)
     * @param name Optional name filter
     * @param lang Optional language filter
     * @param tags Optional tags filter
     * @param isActive Optional active status filter
     * @return List of videos
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<VideoRuResponse>>> searchVideos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "updatedAt") String sort,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) org.ganjp.blog.rubi.model.entity.VideoRu.Language lang,
            @RequestParam(required = false) String tags,
            @RequestParam(required = false) Boolean isActive
    ) {
        try {
            Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) 
                ? Sort.Direction.DESC : Sort.Direction.ASC;
            
            Pageable pageable = PageRequest.of(page, size, sortDirection, sort);
            Page<VideoRuResponse> list = videoRuService.searchVideos(name, lang, tags, isActive, pageable);
            
            PaginatedResponse<VideoRuResponse> response = PaginatedResponse.of(list.getContent(), list.getNumber(), list.getSize(), list.getTotalElements());
            return ResponseEntity.ok(ApiResponse.success(response, "Videos found"));
        } catch (Exception e) {
            log.error("Error searching videos", e);
            return ResponseEntity.status(500).body(ApiResponse.error(500, "Error searching videos: " + e.getMessage(), null));
        }
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<VideoRuResponse>>> listVideos() {
        try {
            List<VideoRuResponse> list = videoRuService.listVideos();
            return ResponseEntity.ok(ApiResponse.success(list, "Videos listed"));
        } catch (Exception e) {
            log.error("Error listing videos", e);
            return ResponseEntity.status(500).body(ApiResponse.error(500, "Error listing videos: " + e.getMessage(), null));
        }
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<VideoRuResponse>> uploadVideo(@Valid @ModelAttribute VideoRuCreateRequest request, HttpServletRequest httpRequest) {
        try {
            String userId = jwtUtils.extractUserIdFromToken(httpRequest);
            VideoRuResponse res = videoRuService.createVideo(request, userId);
            return ResponseEntity.status(201).body(ApiResponse.success(res, "VideoRu uploaded"));
        } catch (IOException e) {
            log.error("Error uploading video", e);
            return ResponseEntity.status(500).body(ApiResponse.error(500, "Error uploading video: " + e.getMessage(), null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage(), null));
        }
    }

    // create-from-URL endpoint removed; file upload is required

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VideoRuResponse>> getVideo(@PathVariable String id) {
        try {
            VideoRuResponse r = videoRuService.getVideoById(id);
            if (r == null) return ResponseEntity.status(404).body(ApiResponse.error(404, "VideoRu not found", null));
            return ResponseEntity.ok(ApiResponse.success(r, "VideoRu found"));
        } catch (Exception e) {
            log.error("Error fetching video", e);
            return ResponseEntity.status(500).body(ApiResponse.error(500, "Error fetching video: " + e.getMessage(), null));
        }
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<VideoRuResponse>> updateVideo(@PathVariable String id, @Valid @ModelAttribute VideoRuUpdateRequest request, HttpServletRequest httpRequest) {
        try {
            String userId = jwtUtils.extractUserIdFromToken(httpRequest);
            VideoRuResponse r = videoRuService.updateVideo(id, request, userId);
            if (r == null) return ResponseEntity.status(404).body(ApiResponse.error(404, "VideoRu not found", null));
            return ResponseEntity.ok(ApiResponse.success(r, "VideoRu updated"));
        } catch (Exception e) {
            log.error("Error updating video", e);
            return ResponseEntity.status(500).body(ApiResponse.error(500, "Error updating video: " + e.getMessage(), null));
        }
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<VideoRuResponse>> updateVideoJson(@PathVariable String id, @Valid @RequestBody VideoRuUpdateRequest request, HttpServletRequest httpRequest) {
        try {
            String userId = jwtUtils.extractUserIdFromToken(httpRequest);
            VideoRuResponse r = videoRuService.updateVideo(id, request, userId);
            if (r == null) return ResponseEntity.status(404).body(ApiResponse.error(404, "VideoRu not found", null));
            return ResponseEntity.ok(ApiResponse.success(r, "VideoRu updated"));
        } catch (Exception e) {
            log.error("Error updating video (json)", e);
            return ResponseEntity.status(500).body(ApiResponse.error(500, "Error updating video: " + e.getMessage(), null));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteVideo(@PathVariable String id, HttpServletRequest httpRequest) {
        try {
            String userId = jwtUtils.extractUserIdFromToken(httpRequest);
            boolean ok = videoRuService.deleteVideo(id, userId);
            if (!ok) return ResponseEntity.status(404).body(ApiResponse.error(404, "VideoRu not found", null));
            return ResponseEntity.ok(ApiResponse.success(null, "VideoRu deleted"));
        } catch (Exception e) {
            log.error("Error deleting video", e);
            return ResponseEntity.status(500).body(ApiResponse.error(500, "Error deleting video: " + e.getMessage(), null));
        }
    }



    // Optional: serve file by filename
    @GetMapping("/view/{filename}")
    public ResponseEntity<?> viewVideo(@PathVariable String filename, @RequestHeader(value = "Range", required = false) String rangeHeader) {
        try {
            java.io.File file = videoRuService.getVideoFileByFilename(filename);
            long contentLength = file.length();
            String contentType = org.ganjp.blog.rubi.util.RubiUtil.determineContentType(filename);

            if (rangeHeader == null) {
                InputStreamResource full = new InputStreamResource(new java.io.FileInputStream(file));
                return ResponseEntity.ok()
                        .contentType(org.springframework.http.MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                        .contentLength(contentLength)
                        .body(full);
            }

            HttpRange httpRange = HttpRange.parseRanges(rangeHeader).get(0);
            long start = httpRange.getRangeStart(contentLength);
            long end = httpRange.getRangeEnd(contentLength);
            long rangeLength = end - start + 1;

            // Stream the requested range without loading entire range into memory
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
                    .contentType(org.springframework.http.MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + contentLength)
                    .contentLength(rangeLength)
                    .body(resource);
        } catch (IllegalArgumentException e) {
            log.error("VideoRu not found: {}", filename, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Error reading video file: {}", filename, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
