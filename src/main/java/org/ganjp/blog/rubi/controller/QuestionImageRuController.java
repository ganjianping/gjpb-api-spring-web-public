package org.ganjp.blog.rubi.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ganjp.blog.auth.security.JwtUtils;
import org.ganjp.blog.rubi.model.dto.QuestionImageRuCreateRequest;
import org.ganjp.blog.rubi.model.dto.QuestionImageRuResponse;
import org.ganjp.blog.rubi.model.dto.QuestionImageRuUpdateRequest;
import org.ganjp.blog.rubi.model.entity.QuestionImageRu;
import org.ganjp.blog.rubi.service.QuestionImageRuService;
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
@RequestMapping("/v1/question-image-rus")
@RequiredArgsConstructor
public class QuestionImageRuController {
    private final QuestionImageRuService questionImageRuService;
    private final JwtUtils jwtUtils;

    @GetMapping("/view/{filename:.+}")
    public ResponseEntity<Resource> viewImage(@PathVariable String filename) {
        Resource file = questionImageRuService.getImage(filename);
        String contentType = determineContentType(filename);
        
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getFilename() + "\"")
                .body(file);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<QuestionImageRuResponse>>> searchQuestionImageRus(
            @RequestParam(required = false) String multipleChoiceQuestionId,
            @RequestParam(required = false) String freeTextQuestionId,
            @RequestParam(required = false) String trueFalseQuestionId,
            @RequestParam(required = false) String fillBlankQuestionId,
            @RequestParam(required = false) QuestionImageRu.Language lang,
            @RequestParam(required = false) Boolean isActive
    ) {
        List<QuestionImageRuResponse> images = questionImageRuService.searchQuestionImageRus(
                multipleChoiceQuestionId, freeTextQuestionId, trueFalseQuestionId, fillBlankQuestionId, lang, isActive);
        return ResponseEntity.ok(ApiResponse.success(images, "Question answer images found"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<QuestionImageRuResponse>> getQuestionAnswerImage(@PathVariable String id) {
        QuestionImageRuResponse image = questionImageRuService.getQuestionImageRuById(id);
        if (image == null) {
            return ResponseEntity.status(404).body(ApiResponse.error(404, "Question answer image not found", null));
        }
        return ResponseEntity.ok(ApiResponse.success(image, "Question answer image found"));
    }

    @GetMapping("/multipleChoiceQuestionRu/{multipleChoiceQuestionId}")
    public ResponseEntity<ApiResponse<List<QuestionImageRuResponse>>> getImagesByMultipleChoiceQuestionRu(@PathVariable String multipleChoiceQuestionId) {
        List<QuestionImageRuResponse> images = questionImageRuService.listQuestionImageRusByMultipleChoiceQuestionRu(multipleChoiceQuestionId);
        return ResponseEntity.ok(ApiResponse.success(images, "Images found for MultipleChoiceQuestion"));
    }

    @GetMapping("/freeTextQuestionRu/{freeTextQuestionId}")
    public ResponseEntity<ApiResponse<List<QuestionImageRuResponse>>> getImagesByFreeTextQuestionRu(@PathVariable String freeTextQuestionId) {
        List<QuestionImageRuResponse> images = questionImageRuService.listQuestionImageRusByFreeTextQuestionRu(freeTextQuestionId);
        return ResponseEntity.ok(ApiResponse.success(images, "Images found for FreeTextQuestion"));
    }

    @GetMapping("/trueFalseQuestionRu/{trueFalseQuestionId}")
    public ResponseEntity<ApiResponse<List<QuestionImageRuResponse>>> getImagesByTrueFalseQuestionRu(@PathVariable String trueFalseQuestionId) {
        List<QuestionImageRuResponse> images = questionImageRuService.listQuestionImageRusByTrueFalseQuestionRu(trueFalseQuestionId);
        return ResponseEntity.ok(ApiResponse.success(images, "Images found for TrueFalseQuestion"));
    }

    @GetMapping("/fillBlankQuestionRu/{fillBlankQuestionId}")
    public ResponseEntity<ApiResponse<List<QuestionImageRuResponse>>> getImagesByFillBlankQuestionRu(@PathVariable String fillBlankQuestionId) {
        List<QuestionImageRuResponse> images = questionImageRuService.listQuestionImageRusByFillBlankQuestionRu(fillBlankQuestionId);
        return ResponseEntity.ok(ApiResponse.success(images, "Images found for FillBlankQuestion"));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<QuestionImageRuResponse>> createQuestionImageRuJson(
            @Valid @RequestBody QuestionImageRuCreateRequest request,
            HttpServletRequest httpRequest
    ) {
        if (request.getOriginalUrl() == null || request.getOriginalUrl().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "Original URL is required", null));
        }
        if (!request.hasQuestionReference()) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "At least one Question ID (MultipleChoice, FreeText, TrueFalse, or FillBlank) is required", null));
        }
        String userId = jwtUtils.extractUserIdFromToken(httpRequest);
        QuestionImageRuResponse image = questionImageRuService.createQuestionImageRu(request, userId);
        return ResponseEntity.ok(ApiResponse.success(image, "Question answer image created"));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<QuestionImageRuResponse>> createQuestionImageRu(
            @Valid @ModelAttribute QuestionImageRuCreateRequest request,
            HttpServletRequest httpRequest
    ) {
        if (!request.hasQuestionReference()) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "At least one Question ID (MultipleChoice, FreeText, TrueFalse, or FillBlank) is required", null));
        }
        String userId = jwtUtils.extractUserIdFromToken(httpRequest);
        QuestionImageRuResponse image = questionImageRuService.createQuestionImageRu(request, userId);
        return ResponseEntity.ok(ApiResponse.success(image, "Question answer image created"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<QuestionImageRuResponse>> updateQuestionImageRu(
            @PathVariable String id,
            @RequestBody QuestionImageRuUpdateRequest request,
            HttpServletRequest httpRequest
    ) {
        String userId = jwtUtils.extractUserIdFromToken(httpRequest);
        QuestionImageRuResponse image = questionImageRuService.updateQuestionImageRu(id, request, userId);
        if (image == null) {
            return ResponseEntity.status(404).body(ApiResponse.error(404, "Question answer image not found", null));
        }
        return ResponseEntity.ok(ApiResponse.success(image, "Question answer image updated"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteQuestionImageRu(@PathVariable String id) {
        questionImageRuService.deleteQuestionImageRu(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Question answer image deleted"));
    }

    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<ApiResponse<Void>> deleteQuestionImageRuPermanently(@PathVariable String id) {
        questionImageRuService.deleteQuestionImageRuPermanently(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Question answer image permanently deleted"));
    }

    private String determineContentType(String filename) {
        if (filename == null || filename.isEmpty()) {
            return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
        
        String extension = "";
        int lastDot = filename.lastIndexOf('.');
        if (lastDot != -1 && lastDot < filename.length() - 1) {
            extension = filename.substring(lastDot + 1).toLowerCase();
        }
        
        return switch (extension) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "svg" -> "image/svg+xml";
            case "webp" -> "image/webp";
            default -> MediaType.APPLICATION_OCTET_STREAM_VALUE;
        };
    }
}
