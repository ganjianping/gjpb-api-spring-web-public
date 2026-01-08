package org.ganjp.blog.rubi.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ganjp.blog.auth.security.JwtUtils;
import org.ganjp.blog.common.model.ApiResponse;
import org.ganjp.blog.common.model.PaginatedResponse;
import org.ganjp.blog.rubi.model.dto.CreateMultipleChoiceQuestionRuRequest;
import org.ganjp.blog.rubi.model.dto.MultipleChoiceQuestionRuResponse;
import org.ganjp.blog.rubi.model.dto.UpdateMultipleChoiceQuestionRuRequest;
import org.ganjp.blog.rubi.model.entity.MultipleChoiceQuestionRu;
import org.ganjp.blog.rubi.service.MultipleChoiceQuestionRuService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/multiple-choice-question-rus")
@RequiredArgsConstructor
public class MultipleChoiceQuestionRuController {

    private final MultipleChoiceQuestionRuService multipleChoiceQuestionRuService;
    private final JwtUtils jwtUtils;

    @PostMapping
    public ResponseEntity<ApiResponse<MultipleChoiceQuestionRuResponse>> createMultipleChoiceQuestionRu(
            @Valid @RequestBody CreateMultipleChoiceQuestionRuRequest request,
            HttpServletRequest httpRequest) {
        String createdBy = extractUserIdFromRequest(httpRequest);
        MultipleChoiceQuestionRuResponse response = multipleChoiceQuestionRuService.createMultipleChoiceQuestionRu(request, createdBy);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response, "MCQ created successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MultipleChoiceQuestionRuResponse>> getMultipleChoiceQuestionRuById(@PathVariable String id) {
        MultipleChoiceQuestionRuResponse response = multipleChoiceQuestionRuService.getMultipleChoiceQuestionRuById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "MCQ retrieved successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<MultipleChoiceQuestionRuResponse>>> getAllMultipleChoiceQuestionRus(
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

        Page<MultipleChoiceQuestionRuResponse> multipleChoiceQuestionRuPage = multipleChoiceQuestionRuService.getAllMultipleChoiceQuestionRus(pageable, lang, difficultyLevel, tags, isActive);

        PaginatedResponse<MultipleChoiceQuestionRuResponse> paginatedResponse = PaginatedResponse.of(multipleChoiceQuestionRuPage);

        return ResponseEntity.ok(ApiResponse.success(paginatedResponse, "MCQs retrieved successfully"));
    }

    @GetMapping("/active/{lang}")
    public ResponseEntity<ApiResponse<List<MultipleChoiceQuestionRuResponse>>> getActiveMultipleChoiceQuestionRusByLang(@PathVariable String lang) {
        try {
            MultipleChoiceQuestionRu.Language language = MultipleChoiceQuestionRu.Language.valueOf(lang.toUpperCase());
            List<MultipleChoiceQuestionRuResponse> responses = multipleChoiceQuestionRuService.getActiveMultipleChoiceQuestionRusByLang(language);
            return ResponseEntity.ok(ApiResponse.success(responses, "MCQs retrieved successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "Invalid language: " + lang, null));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MultipleChoiceQuestionRuResponse>> updateMultipleChoiceQuestionRu(
            @PathVariable String id,
            @Valid @RequestBody UpdateMultipleChoiceQuestionRuRequest request,
            HttpServletRequest httpRequest) {
        String updatedBy = extractUserIdFromRequest(httpRequest);
        MultipleChoiceQuestionRuResponse response = multipleChoiceQuestionRuService.updateMultipleChoiceQuestionRu(id, request, updatedBy);
        return ResponseEntity.ok(ApiResponse.success(response, "MCQ updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMultipleChoiceQuestionRu(
            @PathVariable String id,
            HttpServletRequest httpRequest) {
        String deletedBy = extractUserIdFromRequest(httpRequest);
        multipleChoiceQuestionRuService.deleteMultipleChoiceQuestionRu(id, deletedBy);
        return ResponseEntity.ok(ApiResponse.success(null, "MCQ deleted successfully"));
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