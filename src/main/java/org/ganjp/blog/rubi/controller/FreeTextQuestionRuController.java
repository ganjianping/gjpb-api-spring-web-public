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
@RequestMapping("/v1/ru/saqs")
@RequiredArgsConstructor
public class FreeTextQuestionRuController {

    private final FreeTextQuestionRuService freeTextQuestionRuService;
    private final JwtUtils jwtUtils;

    @PostMapping
    public ResponseEntity<ApiResponse<FreeTextQuestionRuResponse>> createSaq(
            @Valid @RequestBody CreateFreeTextQuestionRuRequest request,
            HttpServletRequest httpRequest) {
        String createdBy = extractUserIdFromRequest(httpRequest);
        FreeTextQuestionRuResponse response = freeTextQuestionRuService.createSaq(request, createdBy);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response, "SAQ created successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FreeTextQuestionRuResponse>> getSaqById(@PathVariable String id) {
        FreeTextQuestionRuResponse response = freeTextQuestionRuService.getSaqById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "SAQ retrieved successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<FreeTextQuestionRuResponse>>> getAllSaqs(
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

        Page<FreeTextQuestionRuResponse> saqPage = freeTextQuestionRuService.getAllSaqs(pageable, lang, difficultyLevel, tags, isActive);

        PaginatedResponse<FreeTextQuestionRuResponse> paginatedResponse = PaginatedResponse.of(saqPage);

        return ResponseEntity.ok(ApiResponse.success(paginatedResponse, "SAQs retrieved successfully"));
    }

    @GetMapping("/active/{lang}")
    public ResponseEntity<ApiResponse<List<FreeTextQuestionRuResponse>>> getActiveSaqsByLang(@PathVariable String lang) {
        try {
            SaqRu.Language language = SaqRu.Language.valueOf(lang.toUpperCase());
            List<FreeTextQuestionRuResponse> responses = freeTextQuestionRuService.getActiveSaqsByLang(language);
            return ResponseEntity.ok(ApiResponse.success(responses, "SAQs retrieved successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "Invalid language: " + lang, null));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FreeTextQuestionRuResponse>> updateSaq(
            @PathVariable String id,
            @Valid @RequestBody UpdateFreeTextQuestionRuRequest request,
            HttpServletRequest httpRequest) {
        String updatedBy = extractUserIdFromRequest(httpRequest);
        FreeTextQuestionRuResponse response = freeTextQuestionRuService.updateSaq(id, request, updatedBy);
        return ResponseEntity.ok(ApiResponse.success(response, "SAQ updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSaq(
            @PathVariable String id,
            HttpServletRequest httpRequest) {
        String deletedBy = extractUserIdFromRequest(httpRequest);
        freeTextQuestionRuService.deleteSaq(id, deletedBy);
        return ResponseEntity.ok(ApiResponse.success(null, "SAQ deleted successfully"));
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