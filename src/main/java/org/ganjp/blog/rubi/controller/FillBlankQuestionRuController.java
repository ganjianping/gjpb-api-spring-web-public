package org.ganjp.blog.rubi.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ganjp.blog.auth.security.JwtUtils;
import org.ganjp.blog.common.model.ApiResponse;
import org.ganjp.blog.common.model.PaginatedResponse;
import org.ganjp.blog.rubi.model.dto.CreateFillBlankQuestionRuRequest;
import org.ganjp.blog.rubi.model.dto.FillBlankQuestionRuResponse;
import org.ganjp.blog.rubi.model.dto.UpdateFillBlankQuestionRuRequest;
import org.ganjp.blog.rubi.model.entity.FillBlankQuestionRu;
import org.ganjp.blog.rubi.service.FillBlankQuestionRuService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/fill-blank-question-rus")
@RequiredArgsConstructor
public class FillBlankQuestionRuController {

    private final FillBlankQuestionRuService fillBlankQuestionRuService;
    private final JwtUtils jwtUtils;

    @PostMapping
    public ResponseEntity<ApiResponse<FillBlankQuestionRuResponse>> createFillBlankQuestionRu(
            @Valid @RequestBody CreateFillBlankQuestionRuRequest request,
            HttpServletRequest httpRequest) {
        String createdBy = extractUserIdFromRequest(httpRequest);
        FillBlankQuestionRuResponse response = fillBlankQuestionRuService.createFillBlankQuestionRu(request, createdBy);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response, "FillBlankQuestion created successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FillBlankQuestionRuResponse>> getFillBlankQuestionRuById(@PathVariable String id) {
        FillBlankQuestionRuResponse response = fillBlankQuestionRuService.getFillBlankQuestionRuById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "FillBlankQuestion retrieved successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<FillBlankQuestionRuResponse>>> getAllFillBlankQuestionRus(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String lang,
            @RequestParam(required = false) String difficultyLevel,
            @RequestParam(required = false) String tags,
            @RequestParam(required = false) Boolean isActive) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<FillBlankQuestionRuResponse> fillBlankQuestionRuPage = fillBlankQuestionRuService.getAllFillBlankQuestionRus(pageable, lang, difficultyLevel, tags, isActive);

        PaginatedResponse<FillBlankQuestionRuResponse> paginatedResponse = PaginatedResponse.of(fillBlankQuestionRuPage);

        return ResponseEntity.ok(ApiResponse.success(paginatedResponse, "FillBlankQuestions retrieved successfully"));
    }

    @GetMapping("/active/{lang}")
    public ResponseEntity<ApiResponse<List<FillBlankQuestionRuResponse>>> getActiveFillBlankQuestionRusByLang(@PathVariable String lang) {
        try {
            FillBlankQuestionRu.Language language = FillBlankQuestionRu.Language.valueOf(lang.toUpperCase());
            List<FillBlankQuestionRuResponse> responses = fillBlankQuestionRuService.getActiveFillBlankQuestionRusByLang(language);
            return ResponseEntity.ok(ApiResponse.success(responses, "FillBlankQuestions retrieved successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "Invalid language: " + lang, null));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FillBlankQuestionRuResponse>> updateFillBlankQuestionRu(
            @PathVariable String id,
            @Valid @RequestBody UpdateFillBlankQuestionRuRequest request,
            HttpServletRequest httpRequest) {
        String updatedBy = extractUserIdFromRequest(httpRequest);
        FillBlankQuestionRuResponse response = fillBlankQuestionRuService.updateFillBlankQuestionRu(id, request, updatedBy);
        return ResponseEntity.ok(ApiResponse.success(response, "FillBlankQuestion updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteFillBlankQuestionRu(
            @PathVariable String id,
            HttpServletRequest httpRequest) {
        String deletedBy = extractUserIdFromRequest(httpRequest);
        fillBlankQuestionRuService.deleteFillBlankQuestionRu(id, deletedBy);
        return ResponseEntity.ok(ApiResponse.success(null, "FillBlankQuestion deleted successfully"));
    }

    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<ApiResponse<Void>> deleteFillBlankQuestionRuPermanently(@PathVariable String id) {
        fillBlankQuestionRuService.deleteFillBlankQuestionRuPermanently(id);
        return ResponseEntity.ok(ApiResponse.success(null, "FillBlankQuestion permanently deleted successfully"));
    }

    private String extractUserIdFromRequest(HttpServletRequest request) {
        try {
            return jwtUtils.extractUserIdFromToken(request);
        } catch (Exception e) {
            // For development/testing purposes, return a default user ID
            return "system";
        }
    }
}
