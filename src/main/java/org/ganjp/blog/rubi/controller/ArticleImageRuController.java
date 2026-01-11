package org.ganjp.blog.rubi.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ganjp.blog.auth.security.JwtUtils;
import org.ganjp.blog.rubi.model.dto.ArticleImageRuCreateRequest;
import org.ganjp.blog.rubi.model.dto.ArticleImageRuResponse;
import org.ganjp.blog.rubi.model.dto.ArticleImageRuUpdateRequest;
import org.ganjp.blog.rubi.model.entity.ArticleImageRu;
import org.ganjp.blog.rubi.service.ArticleImageRuService;
import org.ganjp.blog.common.model.ApiResponse;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/v1/article-image-rus")
@RequiredArgsConstructor
public class ArticleImageRuController {
    private final ArticleImageRuService articleImageRuService;
    private final JwtUtils jwtUtils;

    @GetMapping("/view/{filename:.+}")
    public ResponseEntity<Resource> viewImage(@PathVariable String filename) {
        Resource file = articleImageRuService.getImage(filename);
        String contentType = org.ganjp.blog.rubi.util.RubiUtil.determineContentType(filename);
        
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getFilename() + "\"")
                .body(file);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ArticleImageRuResponse>>> searchArticleImages(
            @RequestParam(required = false) String articleRuId,
            @RequestParam(required = false) ArticleImageRu.Language lang,
            @RequestParam(required = false) Boolean isActive
    ) {
        List<ArticleImageRuResponse> images = articleImageRuService.searchArticleImages(articleRuId, lang, isActive);
        return ResponseEntity.ok(ApiResponse.success(images, "Article images found"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ArticleImageRuResponse>> getArticleImage(@PathVariable String id) {
        ArticleImageRuResponse image = articleImageRuService.getArticleImageById(id);
        if (image == null) {
            return ResponseEntity.status(404).body(ApiResponse.error(404, "Article image not found", null));
        }
        return ResponseEntity.ok(ApiResponse.success(image, "Article image found"));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<ArticleImageRuResponse>> createArticleImageJson(
            @Valid @RequestBody ArticleImageRuCreateRequest request,
            HttpServletRequest httpRequest
    ) {
        if (request.getOriginalUrl() == null || request.getOriginalUrl().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "Original URL is required", null));
        }
        String userId = jwtUtils.extractUserIdFromToken(httpRequest);
        ArticleImageRuResponse image = articleImageRuService.createArticleImage(request, userId);
        return ResponseEntity.ok(ApiResponse.success(image, "Article image created"));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ArticleImageRuResponse>> createArticleImage(
            @Valid @ModelAttribute ArticleImageRuCreateRequest request,
            HttpServletRequest httpRequest
    ) {
        String userId = jwtUtils.extractUserIdFromToken(httpRequest);
        ArticleImageRuResponse image = articleImageRuService.createArticleImage(request, userId);
        return ResponseEntity.ok(ApiResponse.success(image, "Article image created"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ArticleImageRuResponse>> updateArticleImage(
            @PathVariable String id,
            @RequestBody ArticleImageRuUpdateRequest request,
            HttpServletRequest httpRequest
    ) {
        String userId = jwtUtils.extractUserIdFromToken(httpRequest);
        ArticleImageRuResponse image = articleImageRuService.updateArticleImage(id, request, userId);
        if (image == null) {
            return ResponseEntity.status(404).body(ApiResponse.error(404, "Article image not found", null));
        }
        return ResponseEntity.ok(ApiResponse.success(image, "Article image updated"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteArticleImage(@PathVariable String id) {
        articleImageRuService.deleteArticleImage(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Article image deleted"));
    }

    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<ApiResponse<Void>> deleteArticleImagePermanently(@PathVariable String id) {
        articleImageRuService.deleteArticleImagePermanently(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Article image permanently deleted"));
    }
}
