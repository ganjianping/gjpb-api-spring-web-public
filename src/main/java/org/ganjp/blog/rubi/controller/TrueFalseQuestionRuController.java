package org.ganjp.blog.rubi.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ganjp.blog.auth.security.JwtUtils;
import org.ganjp.blog.common.model.ApiResponse;
import org.ganjp.blog.common.model.PaginatedResponse;
import org.ganjp.blog.rubi.model.dto.CreateTrueFalseQuestionRuRequest;
import org.ganjp.blog.rubi.model.dto.TrueFalseQuestionRuResponse;
import org.ganjp.blog.rubi.model.dto.UpdateTrueFalseQuestionRuRequest;
import org.ganjp.blog.rubi.model.entity.TrueFalseQuestionRu;
import org.ganjp.blog.rubi.service.TrueFalseQuestionRuService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/true-false-question-rus")
@RequiredArgsConstructor
public class TrueFalseQuestionRuController {

    private final TrueFalseQuestionRuService trueFalseQuestionRuService;
    private final JwtUtils jwtUtils;

    @PostMapping
    public ResponseEntity<ApiResponse<TrueFalseQuestionRuResponse>> createTrueFalseQuestionRu(
            @Valid @RequestBody CreateTrueFalseQuestionRuRequest request,
            HttpServletRequest httpRequest) {
        String createdBy = extractUserIdFromRequest(httpRequest);
        TrueFalseQuestionRuResponse response = trueFalseQuestionRuService.createTrueFalseQuestionRu(request, createdBy);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response, "TrueFalseQuestion created successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TrueFalseQuestionRuResponse>> getTrueFalseQuestionRuById(@PathVariable String id) {
        TrueFalseQuestionRuResponse response = trueFalseQuestionRuService.getTrueFalseQuestionRuById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "TrueFalseQuestion retrieved successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<TrueFalseQuestionRuResponse>>> getAllTrueFalseQuestionRus(
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

        Page<TrueFalseQuestionRuResponse> trueFalseQuestionRuPage = trueFalseQuestionRuService.getAllTrueFalseQuestionRus(pageable, lang, difficultyLevel, tags, isActive);

        PaginatedResponse<TrueFalseQuestionRuResponse> paginatedResponse = PaginatedResponse.of(trueFalseQuestionRuPage);

        return ResponseEntity.ok(ApiResponse.success(paginatedResponse, "TrueFalseQuestions retrieved successfully"));
    }

    @GetMapping("/active/{lang}")
    public ResponseEntity<ApiResponse<List<TrueFalseQuestionRuResponse>>> getActiveTrueFalseQuestionRusByLang(@PathVariable String lang) {
        try {
            TrueFalseQuestionRu.Language language = TrueFalseQuestionRu.Language.valueOf(lang.toUpperCase());
            List<TrueFalseQuestionRuResponse> responses = trueFalseQuestionRuService.getActiveTrueFalseQuestionRusByLang(language);
            return ResponseEntity.ok(ApiResponse.success(responses, "TrueFalseQuestions retrieved successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "Invalid language: " + lang, null));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TrueFalseQuestionRuResponse>> updateTrueFalseQuestionRu(
            @PathVariable String id,
            @Valid @RequestBody UpdateTrueFalseQuestionRuRequest request,
            HttpServletRequest httpRequest) {
        String updatedBy = extractUserIdFromRequest(httpRequest);
        TrueFalseQuestionRuResponse response = trueFalseQuestionRuService.updateTrueFalseQuestionRu(id, request, updatedBy);
        return ResponseEntity.ok(ApiResponse.success(response, "TrueFalseQuestion updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTrueFalseQuestionRu(
            @PathVariable String id,
            HttpServletRequest httpRequest) {
        String deletedBy = extractUserIdFromRequest(httpRequest);
        trueFalseQuestionRuService.deleteTrueFalseQuestionRu(id, deletedBy);
        return ResponseEntity.ok(ApiResponse.success(null, "TrueFalseQuestion deleted successfully"));
    }

    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<ApiResponse<Void>> deleteTrueFalseQuestionRuPermanently(@PathVariable String id) {
        trueFalseQuestionRuService.deleteTrueFalseQuestionRuPermanently(id);
        return ResponseEntity.ok(ApiResponse.success(null, "TrueFalseQuestion permanently deleted successfully"));
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
