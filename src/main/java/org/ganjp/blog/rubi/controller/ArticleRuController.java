package org.ganjp.blog.rubi.controller;

import lombok.RequiredArgsConstructor;
import org.ganjp.blog.auth.security.JwtUtils;
import org.ganjp.blog.rubi.model.dto.*;
import org.ganjp.blog.rubi.model.entity.ArticleRu;
import org.ganjp.blog.rubi.service.ArticleRuService;
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

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/v1/article-rus")
@RequiredArgsConstructor
public class ArticleRuController {
    private final ArticleRuService articleRuService;
    private final JwtUtils jwtUtils;

    /**
     * Search articles with pagination and filtering
     * GET /v1/articles?title=xxx&lang=EN&tags=yyy&isActive=true&page=0&size=20&sort=updatedAt&direction=desc
     * 
     * @param page Page number (0-based)
     * @param size Page size
     * @param sort Sort field (e.g., updatedAt, createdAt, title)
     * @param direction Sort direction (asc or desc)
     * @param title Optional title filter
     * @param lang Optional language filter
     * @param tags Optional tags filter
     * @param isActive Optional active status filter
     * @return List of articles
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<ArticleRuResponse>>> searchArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "updatedAt") String sort,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) ArticleRu.Language lang,
            @RequestParam(required = false) String tags,
            @RequestParam(required = false) Boolean isActive
    ) {
        try {
            Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) 
                ? Sort.Direction.DESC : Sort.Direction.ASC;
            
            Pageable pageable = PageRequest.of(page, size, sortDirection, sort);
            Page<ArticleRuResponse> list = articleRuService.searchArticles(title, lang, tags, isActive, pageable);
            PaginatedResponse<ArticleRuResponse> response = PaginatedResponse.of(list.getContent(), list.getNumber(), list.getSize(), list.getTotalElements());
            return ResponseEntity.ok(ApiResponse.success(response, "Articles found"));
        } catch (Exception e) {
            log.error("Error searching articles", e);
            return ResponseEntity.status(500).body(ApiResponse.error(500, "Error searching articles: " + e.getMessage(), null));
        }
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<ArticleRuResponse>>> listArticles() {
        try {
            List<ArticleRuResponse> list = articleRuService.listArticles();
            return ResponseEntity.ok(ApiResponse.success(list, "Articles listed"));
        } catch (Exception e) {
            log.error("Error listing articles", e);
            return ResponseEntity.status(500).body(ApiResponse.error(500, "Error listing articles: " + e.getMessage(), null));
        }
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ArticleRuResponse>> createArticle(@Valid @ModelAttribute ArticleRuCreateRequest request, HttpServletRequest httpRequest) {
        try {
            String userId = jwtUtils.extractUserIdFromToken(httpRequest);
            ArticleRuResponse res = articleRuService.createArticle(request, userId);
            return ResponseEntity.status(201).body(ApiResponse.success(res, "ArticleRu created"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error creating article", e);
            return ResponseEntity.status(500).body(ApiResponse.error(500, "Error creating article: " + e.getMessage(), null));
        }
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<ArticleRuResponse>> createArticleJson(@Valid @RequestBody ArticleRuCreateRequest request, HttpServletRequest httpRequest) {
        try {
            String userId = jwtUtils.extractUserIdFromToken(httpRequest);
            ArticleRuResponse res = articleRuService.createArticle(request, userId);
            return ResponseEntity.status(201).body(ApiResponse.success(res, "ArticleRu created"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error creating article (json)", e);
            return ResponseEntity.status(500).body(ApiResponse.error(500, "Error creating article: " + e.getMessage(), null));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ArticleRuResponse>> getArticle(@PathVariable String id) {
        try {
            ArticleRuResponse r = articleRuService.getArticleById(id);
            if (r == null) return ResponseEntity.status(404).body(ApiResponse.error(404, "ArticleRu not found", null));
            return ResponseEntity.ok(ApiResponse.success(r, "ArticleRu found"));
        } catch (Exception e) {
            log.error("Error fetching article", e);
            return ResponseEntity.status(500).body(ApiResponse.error(500, "Error fetching article: " + e.getMessage(), null));
        }
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ArticleRuResponse>> updateArticle(@PathVariable String id, @Valid @ModelAttribute ArticleRuUpdateRequest request, HttpServletRequest httpRequest) {
        try {
            String userId = jwtUtils.extractUserIdFromToken(httpRequest);
            ArticleRuResponse r = articleRuService.updateArticle(id, request, userId);
            if (r == null) return ResponseEntity.status(404).body(ApiResponse.error(404, "ArticleRu not found", null));
            return ResponseEntity.ok(ApiResponse.success(r, "ArticleRu updated"));
        } catch (Exception e) {
            log.error("Error updating article", e);
            return ResponseEntity.status(500).body(ApiResponse.error(500, "Error updating article: " + e.getMessage(), null));
        }
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<ArticleRuResponse>> updateArticleJson(@PathVariable String id, @Valid @RequestBody ArticleRuUpdateRequest request, HttpServletRequest httpRequest) {
        try {
            String userId = jwtUtils.extractUserIdFromToken(httpRequest);
            ArticleRuResponse r = articleRuService.updateArticle(id, request, userId);
            if (r == null) return ResponseEntity.status(404).body(ApiResponse.error(404, "ArticleRu not found", null));
            return ResponseEntity.ok(ApiResponse.success(r, "ArticleRu updated"));
        } catch (Exception e) {
            log.error("Error updating article (json)", e);
            return ResponseEntity.status(500).body(ApiResponse.error(500, "Error updating article: " + e.getMessage(), null));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteArticle(@PathVariable String id, HttpServletRequest httpRequest) {
        try {
            String userId = jwtUtils.extractUserIdFromToken(httpRequest);
            boolean ok = articleRuService.deleteArticle(id, userId);
            if (!ok) return ResponseEntity.status(404).body(ApiResponse.error(404, "ArticleRu not found", null));
            return ResponseEntity.ok(ApiResponse.success(null, "ArticleRu deleted"));
        } catch (Exception e) {
            log.error("Error deleting article", e);
            return ResponseEntity.status(500).body(ApiResponse.error(500, "Error deleting article: " + e.getMessage(), null));
        }
    }

    // serve cover image (supports Range)
    @GetMapping("/cover/{filename}")
    public ResponseEntity<?> viewCover(@PathVariable String filename, @RequestHeader(value = "Range", required = false) String rangeHeader) {
        try {
            java.io.File file = articleRuService.getCoverImageFileByFilename(filename);
            long contentLength = file.length();
            String contentType = org.ganjp.blog.rubi.util.RubiUtil.determineContentType(filename);

            if (rangeHeader == null) {
                InputStreamResource full = new InputStreamResource(new java.io.FileInputStream(file));
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
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
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + contentLength)
                    .contentLength(rangeLength)
                    .body(resource);
        } catch (IllegalArgumentException e) {
            log.error("Cover not found: {}", filename, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Error reading cover file: {}", filename, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
