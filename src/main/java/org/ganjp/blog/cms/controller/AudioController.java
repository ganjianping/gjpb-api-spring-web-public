package org.ganjp.blog.cms.controller;

import lombok.RequiredArgsConstructor;
import org.ganjp.blog.auth.security.JwtUtils;
import org.ganjp.blog.cms.model.dto.*;
import org.ganjp.blog.cms.model.entity.Audio;
import org.ganjp.blog.cms.service.AudioService;
import org.ganjp.blog.common.model.ApiResponse;
import org.springframework.core.io.InputStreamResource;
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
@RequestMapping("/v1/audios")
@RequiredArgsConstructor
public class AudioController {
    private final AudioService audioService;
    private final JwtUtils jwtUtils;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AudioResponse>>> searchAudios(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Audio.Language lang,
            @RequestParam(required = false) String tags,
            @RequestParam(required = false) Boolean isActive
    ) {
        try {
            List<AudioResponse> list = audioService.searchAudios(name, lang, tags, isActive);
            return ResponseEntity.ok(ApiResponse.success(list, "Audios found"));
        } catch (Exception e) {
            log.error("Error searching audios", e);
            return ResponseEntity.status(500).body(ApiResponse.error(500, "Error searching audios: " + e.getMessage(), null));
        }
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<AudioResponse>>> listAudios() {
        try {
            List<AudioResponse> list = audioService.listAudios();
            return ResponseEntity.ok(ApiResponse.success(list, "Audios listed"));
        } catch (Exception e) {
            log.error("Error listing audios", e);
            return ResponseEntity.status(500).body(ApiResponse.error(500, "Error listing audios: " + e.getMessage(), null));
        }
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<AudioResponse>> uploadAudio(@Valid @ModelAttribute AudioCreateRequest request, HttpServletRequest httpRequest) {
        try {
            String userId = jwtUtils.extractUserIdFromToken(httpRequest);
            AudioResponse res = audioService.createAudio(request, userId);
            return ResponseEntity.status(201).body(ApiResponse.success(res, "Audio uploaded"));
        } catch (IOException e) {
            log.error("Error uploading audio", e);
            return ResponseEntity.status(500).body(ApiResponse.error(500, "Error uploading audio: " + e.getMessage(), null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage(), null));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AudioResponse>> getAudio(@PathVariable String id) {
        try {
            AudioResponse r = audioService.getAudioById(id);
            if (r == null) return ResponseEntity.status(404).body(ApiResponse.error(404, "Audio not found", null));
            return ResponseEntity.ok(ApiResponse.success(r, "Audio found"));
        } catch (Exception e) {
            log.error("Error fetching audio", e);
            return ResponseEntity.status(500).body(ApiResponse.error(500, "Error fetching audio: " + e.getMessage(), null));
        }
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<AudioResponse>> updateAudio(@PathVariable String id, @Valid @ModelAttribute AudioUpdateRequest request, HttpServletRequest httpRequest) {
        try {
            String userId = jwtUtils.extractUserIdFromToken(httpRequest);
            AudioResponse r = audioService.updateAudio(id, request, userId);
            if (r == null) return ResponseEntity.status(404).body(ApiResponse.error(404, "Audio not found", null));
            return ResponseEntity.ok(ApiResponse.success(r, "Audio updated"));
        } catch (Exception e) {
            log.error("Error updating audio", e);
            return ResponseEntity.status(500).body(ApiResponse.error(500, "Error updating audio: " + e.getMessage(), null));
        }
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<AudioResponse>> updateAudioJson(@PathVariable String id, @Valid @RequestBody AudioUpdateRequest request, HttpServletRequest httpRequest) {
        try {
            String userId = jwtUtils.extractUserIdFromToken(httpRequest);
            AudioResponse r = audioService.updateAudio(id, request, userId);
            if (r == null) return ResponseEntity.status(404).body(ApiResponse.error(404, "Audio not found", null));
            return ResponseEntity.ok(ApiResponse.success(r, "Audio updated"));
        } catch (Exception e) {
            log.error("Error updating audio (json)", e);
            return ResponseEntity.status(500).body(ApiResponse.error(500, "Error updating audio: " + e.getMessage(), null));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAudio(@PathVariable String id, HttpServletRequest httpRequest) {
        try {
            String userId = jwtUtils.extractUserIdFromToken(httpRequest);
            boolean ok = audioService.deleteAudio(id, userId);
            if (!ok) return ResponseEntity.status(404).body(ApiResponse.error(404, "Audio not found", null));
            return ResponseEntity.ok(ApiResponse.success(null, "Audio deleted"));
        } catch (Exception e) {
            log.error("Error deleting audio", e);
            return ResponseEntity.status(500).body(ApiResponse.error(500, "Error deleting audio: " + e.getMessage(), null));
        }
    }

    // serve file by filename (supports Range)
    @GetMapping("/view/{filename}")
    public ResponseEntity<?> viewAudio(@PathVariable String filename, @RequestHeader(value = "Range", required = false) String rangeHeader) {
        try {
            java.io.File file = audioService.getAudioFileByFilename(filename);
            long contentLength = file.length();
            String contentType = org.ganjp.blog.cms.util.CmsUtil.determineContentType(filename);

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
            log.error("Audio not found: {}", filename, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Error reading audio file: {}", filename, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
