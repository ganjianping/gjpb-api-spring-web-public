package org.ganjp.blog.rubi.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ganjp.blog.auth.security.JwtUtils;
import org.ganjp.blog.rubi.model.dto.QuestionAnswerImageCreateRequest;
import org.ganjp.blog.rubi.model.dto.QuestionAnswerImageResponse;
import org.ganjp.blog.rubi.model.dto.QuestionAnswerImageUpdateRequest;
import org.ganjp.blog.rubi.model.entity.QuestionAnswerImage;
import org.ganjp.blog.rubi.service.QuestionAnswerImageService;
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
@RequestMapping("/v1/rubi/question-images")
@RequiredArgsConstructor
public class QuestionAnswerImageController {
    private final QuestionAnswerImageService questionAnswerImageService;
    private final JwtUtils jwtUtils;

    @GetMapping("/view/{filename:.+}")
    public ResponseEntity<Resource> viewImage(@PathVariable String filename) {
        Resource file = questionAnswerImageService.getImage(filename);
        String contentType = determineContentType(filename);
        
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getFilename() + "\"")
                .body(file);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<QuestionAnswerImageResponse>>> searchQuestionAnswerImages(
            @RequestParam(required = false) String mcqId,
            @RequestParam(required = false) String saqId,
            @RequestParam(required = false) QuestionAnswerImage.Language lang,
            @RequestParam(required = false) Boolean isActive
    ) {
        List<QuestionAnswerImageResponse> images = questionAnswerImageService.searchQuestionAnswerImages(mcqId, saqId, lang, isActive);
        return ResponseEntity.ok(ApiResponse.success(images, "Question answer images found"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<QuestionAnswerImageResponse>> getQuestionAnswerImage(@PathVariable String id) {
        QuestionAnswerImageResponse image = questionAnswerImageService.getQuestionAnswerImageById(id);
        if (image == null) {
            return ResponseEntity.status(404).body(ApiResponse.error(404, "Question answer image not found", null));
        }
        return ResponseEntity.ok(ApiResponse.success(image, "Question answer image found"));
    }

    @GetMapping("/mcq/{mcqId}")
    public ResponseEntity<ApiResponse<List<QuestionAnswerImageResponse>>> getImagesByMcq(@PathVariable String mcqId) {
        List<QuestionAnswerImageResponse> images = questionAnswerImageService.listQuestionAnswerImagesByMcq(mcqId);
        return ResponseEntity.ok(ApiResponse.success(images, "Images found for MCQ"));
    }

    @GetMapping("/saq/{saqId}")
    public ResponseEntity<ApiResponse<List<QuestionAnswerImageResponse>>> getImagesBySaq(@PathVariable String saqId) {
        List<QuestionAnswerImageResponse> images = questionAnswerImageService.listQuestionAnswerImagesBySaq(saqId);
        return ResponseEntity.ok(ApiResponse.success(images, "Images found for SAQ"));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<QuestionAnswerImageResponse>> createQuestionAnswerImageJson(
            @Valid @RequestBody QuestionAnswerImageCreateRequest request,
            HttpServletRequest httpRequest
    ) {
        if (request.getOriginalUrl() == null || request.getOriginalUrl().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "Original URL is required", null));
        }
        if (!request.hasQuestionReference()) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "Either MCQ ID or SAQ ID is required", null));
        }
        String userId = jwtUtils.extractUserIdFromToken(httpRequest);
        QuestionAnswerImageResponse image = questionAnswerImageService.createQuestionAnswerImage(request, userId);
        return ResponseEntity.ok(ApiResponse.success(image, "Question answer image created"));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<QuestionAnswerImageResponse>> createQuestionAnswerImage(
            @Valid @ModelAttribute QuestionAnswerImageCreateRequest request,
            HttpServletRequest httpRequest
    ) {
        if (!request.hasQuestionReference()) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "Either MCQ ID or SAQ ID is required", null));
        }
        String userId = jwtUtils.extractUserIdFromToken(httpRequest);
        QuestionAnswerImageResponse image = questionAnswerImageService.createQuestionAnswerImage(request, userId);
        return ResponseEntity.ok(ApiResponse.success(image, "Question answer image created"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<QuestionAnswerImageResponse>> updateQuestionAnswerImage(
            @PathVariable String id,
            @RequestBody QuestionAnswerImageUpdateRequest request,
            HttpServletRequest httpRequest
    ) {
        String userId = jwtUtils.extractUserIdFromToken(httpRequest);
        QuestionAnswerImageResponse image = questionAnswerImageService.updateQuestionAnswerImage(id, request, userId);
        if (image == null) {
            return ResponseEntity.status(404).body(ApiResponse.error(404, "Question answer image not found", null));
        }
        return ResponseEntity.ok(ApiResponse.success(image, "Question answer image updated"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteQuestionAnswerImage(@PathVariable String id) {
        questionAnswerImageService.deleteQuestionAnswerImage(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Question answer image deleted"));
    }

    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<ApiResponse<Void>> deleteQuestionAnswerImagePermanently(@PathVariable String id) {
        questionAnswerImageService.deleteQuestionAnswerImagePermanently(id);
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
