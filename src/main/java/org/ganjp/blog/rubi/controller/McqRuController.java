package org.ganjp.blog.rubi.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ganjp.blog.auth.security.JwtUtils;
import org.ganjp.blog.common.model.ApiResponse;
import org.ganjp.blog.common.model.PaginatedResponse;
import org.ganjp.blog.rubi.model.dto.CreateMcqRuRequest;
import org.ganjp.blog.rubi.model.dto.McqRuResponse;
import org.ganjp.blog.rubi.model.dto.UpdateMcqRuRequest;
import org.ganjp.blog.rubi.model.entity.McqRu;
import org.ganjp.blog.rubi.service.McqRuService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/ru/mcqs")
@RequiredArgsConstructor
public class McqRuController {

    private final McqRuService mcqRuService;
    private final JwtUtils jwtUtils;

    @PostMapping
    public ResponseEntity<ApiResponse<McqRuResponse>> createMcq(
            @Valid @RequestBody CreateMcqRuRequest request,
            HttpServletRequest httpRequest) {
        String createdBy = extractUserIdFromRequest(httpRequest);
        McqRuResponse response = mcqRuService.createMcq(request, createdBy);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response, "MCQ created successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<McqRuResponse>> getMcqById(@PathVariable String id) {
        McqRuResponse response = mcqRuService.getMcqById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "MCQ retrieved successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<McqRuResponse>>> getAllMcqs(
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

        Page<McqRuResponse> mcqPage = mcqRuService.getAllMcqs(pageable, lang, difficultyLevel, tags, isActive);

        PaginatedResponse<McqRuResponse> paginatedResponse = PaginatedResponse.of(mcqPage);

        return ResponseEntity.ok(ApiResponse.success(paginatedResponse, "MCQs retrieved successfully"));
    }

    @GetMapping("/active/{lang}")
    public ResponseEntity<ApiResponse<List<McqRuResponse>>> getActiveMcqsByLang(@PathVariable String lang) {
        try {
            McqRu.Language language = McqRu.Language.valueOf(lang.toUpperCase());
            List<McqRuResponse> responses = mcqRuService.getActiveMcqsByLang(language);
            return ResponseEntity.ok(ApiResponse.success(responses, "MCQs retrieved successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "Invalid language: " + lang, null));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<McqRuResponse>> updateMcq(
            @PathVariable String id,
            @Valid @RequestBody UpdateMcqRuRequest request,
            HttpServletRequest httpRequest) {
        String updatedBy = extractUserIdFromRequest(httpRequest);
        McqRuResponse response = mcqRuService.updateMcq(id, request, updatedBy);
        return ResponseEntity.ok(ApiResponse.success(response, "MCQ updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMcq(
            @PathVariable String id,
            HttpServletRequest httpRequest) {
        String deletedBy = extractUserIdFromRequest(httpRequest);
        mcqRuService.deleteMcq(id, deletedBy);
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