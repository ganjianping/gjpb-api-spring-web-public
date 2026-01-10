package org.ganjp.blog.rubi.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ganjp.blog.auth.security.JwtUtils;
import org.ganjp.blog.common.model.ApiResponse;
import org.ganjp.blog.common.model.PaginatedResponse;
import org.ganjp.blog.rubi.model.dto.CreateFreeTextQuestionRuRequest;
import org.ganjp.blog.rubi.model.dto.FreeTextQuestionRuResponse;
import org.ganjp.blog.rubi.model.dto.UpdateFreeTextQuestionRuRequest;
import org.ganjp.blog.rubi.model.entity.FreeTextQuestionRu;
import org.ganjp.blog.rubi.service.FreeTextQuestionRuService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/free-text-question-rus")
@RequiredArgsConstructor
public class FreeTextQuestionRuController {

    private final FreeTextQuestionRuService freeTextQuestionRuService;
    private final JwtUtils jwtUtils;

    @PostMapping
    public ResponseEntity<ApiResponse<FreeTextQuestionRuResponse>> createFreeTextQuestionRu(
            @Valid @RequestBody CreateFreeTextQuestionRuRequest request,
            HttpServletRequest httpRequest) {
        String createdBy = extractUserIdFromRequest(httpRequest);
        FreeTextQuestionRuResponse response = freeTextQuestionRuService.createFreeTextQuestionRu(request, createdBy);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response, "FreeTextQuestion created successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FreeTextQuestionRuResponse>> getFreeTextQuestionRuById(@PathVariable String id) {
        FreeTextQuestionRuResponse response = freeTextQuestionRuService.getFreeTextQuestionRuById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "FreeTextQuestion retrieved successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<FreeTextQuestionRuResponse>>> getAllFreeTextQuestionRus(
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

        Page<FreeTextQuestionRuResponse> freeTextQuestionRuPage = freeTextQuestionRuService.getAllFreeTextQuestionRus(pageable, lang, difficultyLevel, tags, isActive);

        PaginatedResponse<FreeTextQuestionRuResponse> paginatedResponse = PaginatedResponse.of(freeTextQuestionRuPage);

        return ResponseEntity.ok(ApiResponse.success(paginatedResponse, "FreeTextQuestions retrieved successfully"));
    }

    @GetMapping("/active/{lang}")
    public ResponseEntity<ApiResponse<List<FreeTextQuestionRuResponse>>> getActiveFreeTextQuestionRusByLang(@PathVariable String lang) {
        try {
            FreeTextQuestionRu.Language language = FreeTextQuestionRu.Language.valueOf(lang.toUpperCase());
            List<FreeTextQuestionRuResponse> responses = freeTextQuestionRuService.getActiveFreeTextQuestionRusByLang(language);
            return ResponseEntity.ok(ApiResponse.success(responses, "FreeTextQuestions retrieved successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "Invalid language: " + lang, null));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FreeTextQuestionRuResponse>> updateFreeTextQuestionRu(
            @PathVariable String id,
            @Valid @RequestBody UpdateFreeTextQuestionRuRequest request,
            HttpServletRequest httpRequest) {
        String updatedBy = extractUserIdFromRequest(httpRequest);
        FreeTextQuestionRuResponse response = freeTextQuestionRuService.updateFreeTextQuestionRu(id, request, updatedBy);
        return ResponseEntity.ok(ApiResponse.success(response, "FreeTextQuestion updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteFreeTextQuestionRu(
            @PathVariable String id,
            HttpServletRequest httpRequest) {
        String deletedBy = extractUserIdFromRequest(httpRequest);
        freeTextQuestionRuService.deleteFreeTextQuestionRu(id, deletedBy);
        return ResponseEntity.ok(ApiResponse.success(null, "FreeTextQuestion deleted successfully"));
    }

    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<ApiResponse<Void>> deleteFreeTextQuestionRuPermanently(@PathVariable String id) {
        freeTextQuestionRuService.deleteFreeTextQuestionRuPermanently(id);
        return ResponseEntity.ok(ApiResponse.success(null, "FreeTextQuestion permanently deleted successfully"));
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