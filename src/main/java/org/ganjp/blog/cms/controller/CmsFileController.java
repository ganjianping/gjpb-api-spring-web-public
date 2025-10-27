package org.ganjp.blog.cms.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ganjp.blog.cms.model.dto.*;
import org.ganjp.blog.cms.service.CmsFileService;
import org.ganjp.blog.auth.security.JwtUtils;
import org.ganjp.blog.common.model.ApiResponse;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/v1/files")
@RequiredArgsConstructor
@Slf4j
public class CmsFileController {
    private final CmsFileService cmsFileService;
    private final JwtUtils jwtUtils;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<CmsFileResponse>> createFile(@Valid @ModelAttribute CmsFileCreateRequest request, HttpServletRequest httpRequest) {
        try {
            String userId = jwtUtils.extractUserIdFromToken(httpRequest);
            CmsFileResponse r = cmsFileService.createFile(request, userId);
            return ResponseEntity.status(201).body(ApiResponse.success(r, "File created"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error creating file", e);
            return ResponseEntity.status(500).body(ApiResponse.error(500, "Error creating file: " + e.getMessage(), null));
        }
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<CmsFileResponse>> createFileJson(@Valid @RequestBody CmsFileCreateRequest request, HttpServletRequest httpRequest) {
        try {
            String userId = jwtUtils.extractUserIdFromToken(httpRequest);
            CmsFileResponse r = cmsFileService.createFile(request, userId);
            return ResponseEntity.status(201).body(ApiResponse.success(r, "File created"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error creating file (json)", e);
            return ResponseEntity.status(500).body(ApiResponse.error(500, "Error creating file: " + e.getMessage(), null));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CmsFileResponse>> getFile(@PathVariable String id) {
        try {
            CmsFileResponse r = cmsFileService.getFileById(id);
            if (r == null) return ResponseEntity.status(404).body(ApiResponse.error(404, "File not found", null));
            return ResponseEntity.ok(ApiResponse.success(r, "File found"));
        } catch (Exception e) {
            log.error("Error fetching file", e);
            return ResponseEntity.status(500).body(ApiResponse.error(500, "Error fetching file: " + e.getMessage(), null));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CmsFileResponse>>> listFiles() {
        try {
            List<CmsFileResponse> list = cmsFileService.listFiles();
            return ResponseEntity.ok(ApiResponse.success(list, "Files listed"));
        } catch (Exception e) {
            log.error("Error listing files", e);
            return ResponseEntity.status(500).body(ApiResponse.error(500, "Error listing files: " + e.getMessage(), null));
        }
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<CmsFileResponse>> updateFile(@PathVariable String id, @Valid @ModelAttribute CmsFileUpdateRequest request, HttpServletRequest httpRequest) {
        try {
            String userId = jwtUtils.extractUserIdFromToken(httpRequest);
            CmsFileResponse r = cmsFileService.updateFile(id, request, userId);
            if (r == null) return ResponseEntity.status(404).body(ApiResponse.error(404, "File not found", null));
            return ResponseEntity.ok(ApiResponse.success(r, "File updated"));
        } catch (Exception e) {
            log.error("Error updating file", e);
            return ResponseEntity.status(500).body(ApiResponse.error(500, "Error updating file: " + e.getMessage(), null));
        }
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<CmsFileResponse>> updateFileJson(@PathVariable String id, @Valid @RequestBody CmsFileUpdateRequest request, HttpServletRequest httpRequest) {
        try {
            String userId = jwtUtils.extractUserIdFromToken(httpRequest);
            CmsFileResponse r = cmsFileService.updateFile(id, request, userId);
            if (r == null) return ResponseEntity.status(404).body(ApiResponse.error(404, "File not found", null));
            return ResponseEntity.ok(ApiResponse.success(r, "File updated"));
        } catch (Exception e) {
            log.error("Error updating file (json)", e);
            return ResponseEntity.status(500).body(ApiResponse.error(500, "Error updating file: " + e.getMessage(), null));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteFile(@PathVariable String id, HttpServletRequest httpRequest) {
        try {
            String userId = jwtUtils.extractUserIdFromToken(httpRequest);
            boolean ok = cmsFileService.deleteFile(id, userId);
            if (!ok) return ResponseEntity.status(404).body(ApiResponse.error(404, "File not found", null));
            return ResponseEntity.ok(ApiResponse.success(null, "File deleted"));
        } catch (Exception e) {
            log.error("Error deleting file", e);
            return ResponseEntity.status(500).body(ApiResponse.error(500, "Error deleting file: " + e.getMessage(), null));
        }
    }

    // download file by filename (secured)
    @GetMapping("/download/{filename}")
    public ResponseEntity<?> downloadByFilename(@PathVariable String filename) {
        try {
            java.io.File file = cmsFileService.getFileByFilename(filename);
            java.io.InputStream is = new java.io.FileInputStream(file);
            InputStreamResource resource = new InputStreamResource(is);
            String ct = org.ganjp.blog.cms.util.CmsUtil.determineContentType(filename);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(ct))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentLength(file.length())
                    .body(resource);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IOException e) {
            log.error("Error reading file: {}", filename, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
