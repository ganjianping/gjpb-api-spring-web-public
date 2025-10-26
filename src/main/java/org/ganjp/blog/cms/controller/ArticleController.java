package org.ganjp.blog.cms.controller;

import lombok.RequiredArgsConstructor;
import org.ganjp.blog.auth.security.JwtUtils;
import org.ganjp.blog.cms.model.dto.*;
import org.ganjp.blog.cms.service.ArticleService;
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
@RequestMapping("/v1/articles")
@RequiredArgsConstructor
public class ArticleController {
    private final ArticleService articleService;
    private final JwtUtils jwtUtils;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ArticleResponse>> createArticle(@Valid @ModelAttribute ArticleCreateRequest request, HttpServletRequest httpRequest) {
        try {
            String userId = jwtUtils.extractUserIdFromToken(httpRequest);
            ArticleResponse res = articleService.createArticle(request, userId);
            return ResponseEntity.status(201).body(ApiResponse.success(res, "Article created"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error creating article", e);
            return ResponseEntity.status(500).body(ApiResponse.error(500, "Error creating article: " + e.getMessage(), null));
        }
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<ArticleResponse>> createArticleJson(@Valid @RequestBody ArticleCreateRequest request, HttpServletRequest httpRequest) {
        try {
            String userId = jwtUtils.extractUserIdFromToken(httpRequest);
            ArticleResponse res = articleService.createArticle(request, userId);
            return ResponseEntity.status(201).body(ApiResponse.success(res, "Article created"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error creating article (json)", e);
            return ResponseEntity.status(500).body(ApiResponse.error(500, "Error creating article: " + e.getMessage(), null));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ArticleResponse>> getArticle(@PathVariable String id) {
        try {
            ArticleResponse r = articleService.getArticleById(id);
            if (r == null) return ResponseEntity.status(404).body(ApiResponse.error(404, "Article not found", null));
            return ResponseEntity.ok(ApiResponse.success(r, "Article found"));
        } catch (Exception e) {
            log.error("Error fetching article", e);
            return ResponseEntity.status(500).body(ApiResponse.error(500, "Error fetching article: " + e.getMessage(), null));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ArticleResponse>>> listArticles() {
        try {
            List<ArticleResponse> list = articleService.listArticles();
            return ResponseEntity.ok(ApiResponse.success(list, "Articles listed"));
        } catch (Exception e) {
            log.error("Error listing articles", e);
            return ResponseEntity.status(500).body(ApiResponse.error(500, "Error listing articles: " + e.getMessage(), null));
        }
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ArticleResponse>> updateArticle(@PathVariable String id, @Valid @ModelAttribute ArticleUpdateRequest request, HttpServletRequest httpRequest) {
        try {
            String userId = jwtUtils.extractUserIdFromToken(httpRequest);
            ArticleResponse r = articleService.updateArticle(id, request, userId);
            if (r == null) return ResponseEntity.status(404).body(ApiResponse.error(404, "Article not found", null));
            return ResponseEntity.ok(ApiResponse.success(r, "Article updated"));
        } catch (Exception e) {
            log.error("Error updating article", e);
            return ResponseEntity.status(500).body(ApiResponse.error(500, "Error updating article: " + e.getMessage(), null));
        }
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<ArticleResponse>> updateArticleJson(@PathVariable String id, @Valid @RequestBody ArticleUpdateRequest request, HttpServletRequest httpRequest) {
        try {
            String userId = jwtUtils.extractUserIdFromToken(httpRequest);
            ArticleResponse r = articleService.updateArticle(id, request, userId);
            if (r == null) return ResponseEntity.status(404).body(ApiResponse.error(404, "Article not found", null));
            return ResponseEntity.ok(ApiResponse.success(r, "Article updated"));
        } catch (Exception e) {
            log.error("Error updating article (json)", e);
            return ResponseEntity.status(500).body(ApiResponse.error(500, "Error updating article: " + e.getMessage(), null));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteArticle(@PathVariable String id, HttpServletRequest httpRequest) {
        try {
            String userId = jwtUtils.extractUserIdFromToken(httpRequest);
            boolean ok = articleService.deleteArticle(id, userId);
            if (!ok) return ResponseEntity.status(404).body(ApiResponse.error(404, "Article not found", null));
            return ResponseEntity.ok(ApiResponse.success(null, "Article deleted"));
        } catch (Exception e) {
            log.error("Error deleting article", e);
            return ResponseEntity.status(500).body(ApiResponse.error(500, "Error deleting article: " + e.getMessage(), null));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ArticleResponse>>> searchArticles(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) org.ganjp.blog.cms.model.entity.Article.Language lang,
            @RequestParam(required = false) String tags,
            @RequestParam(required = false) Boolean isActive
    ) {
        try {
            List<ArticleResponse> list = articleService.searchArticles(title, lang, tags, isActive);
            return ResponseEntity.ok(ApiResponse.success(list, "Articles found"));
        } catch (Exception e) {
            log.error("Error searching articles", e);
            return ResponseEntity.status(500).body(ApiResponse.error(500, "Error searching articles: " + e.getMessage(), null));
        }
    }

    // serve cover image (supports Range)
    @GetMapping("/cover/{filename}")
    public ResponseEntity<?> viewCover(@PathVariable String filename, @RequestHeader(value = "Range", required = false) String rangeHeader) {
        try {
            java.io.File file = articleService.getCoverImageFileByFilename(filename);
            long contentLength = file.length();
            String contentType = org.ganjp.blog.cms.util.CmsUtil.determineContentType(filename);

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
