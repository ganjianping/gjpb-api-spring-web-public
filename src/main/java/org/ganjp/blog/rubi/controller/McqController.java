package org.ganjp.blog.rubi.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ganjp.blog.auth.security.JwtUtils;
import org.ganjp.blog.common.model.ApiResponse;
import org.ganjp.blog.common.model.PaginatedResponse;
import org.ganjp.blog.rubi.model.dto.CreateMcqRequest;
import org.ganjp.blog.rubi.model.dto.McqResponse;
import org.ganjp.blog.rubi.model.dto.UpdateMcqRequest;
import org.ganjp.blog.rubi.model.entity.Mcq;
import org.ganjp.blog.rubi.service.McqService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/rubi/mcqs")
@RequiredArgsConstructor
public class McqController {

    private final McqService mcqService;
    private final JwtUtils jwtUtils;

    @PostMapping
    public ResponseEntity<ApiResponse<McqResponse>> createMcq(
            @Valid @RequestBody CreateMcqRequest request,
            HttpServletRequest httpRequest) {
        String createdBy = extractUserIdFromRequest(httpRequest);
        McqResponse response = mcqService.createMcq(request, createdBy);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response, "MCQ created successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<McqResponse>> getMcqById(@PathVariable String id) {
        McqResponse response = mcqService.getMcqById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "MCQ retrieved successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<McqResponse>>> getAllMcqs(
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

        Page<McqResponse> mcqPage = mcqService.getAllMcqs(pageable, lang, difficultyLevel, tags, isActive);

        PaginatedResponse<McqResponse> paginatedResponse = PaginatedResponse.of(mcqPage);

        return ResponseEntity.ok(ApiResponse.success(paginatedResponse, "MCQs retrieved successfully"));
    }

    @GetMapping("/active/{lang}")
    public ResponseEntity<ApiResponse<List<McqResponse>>> getActiveMcqsByLang(@PathVariable String lang) {
        try {
            Mcq.Language language = Mcq.Language.valueOf(lang.toUpperCase());
            List<McqResponse> responses = mcqService.getActiveMcqsByLang(language);
            return ResponseEntity.ok(ApiResponse.success(responses, "MCQs retrieved successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "Invalid language: " + lang, null));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<McqResponse>> updateMcq(
            @PathVariable String id,
            @Valid @RequestBody UpdateMcqRequest request,
            HttpServletRequest httpRequest) {
        String updatedBy = extractUserIdFromRequest(httpRequest);
        McqResponse response = mcqService.updateMcq(id, request, updatedBy);
        return ResponseEntity.ok(ApiResponse.success(response, "MCQ updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMcq(
            @PathVariable String id,
            HttpServletRequest httpRequest) {
        String deletedBy = extractUserIdFromRequest(httpRequest);
        mcqService.deleteMcq(id, deletedBy);
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