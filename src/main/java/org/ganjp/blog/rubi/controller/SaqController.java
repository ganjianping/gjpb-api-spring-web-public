package org.ganjp.blog.rubi.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ganjp.blog.auth.security.JwtUtils;
import org.ganjp.blog.common.model.ApiResponse;
import org.ganjp.blog.common.model.PaginatedResponse;
import org.ganjp.blog.rubi.model.dto.CreateSaqRequest;
import org.ganjp.blog.rubi.model.dto.SaqResponse;
import org.ganjp.blog.rubi.model.dto.UpdateSaqRequest;
import org.ganjp.blog.rubi.model.entity.Saq;
import org.ganjp.blog.rubi.service.SaqService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/rubi/saqs")
@RequiredArgsConstructor
public class SaqController {

    private final SaqService saqService;
    private final JwtUtils jwtUtils;

    @PostMapping
    public ResponseEntity<ApiResponse<SaqResponse>> createSaq(
            @Valid @RequestBody CreateSaqRequest request,
            HttpServletRequest httpRequest) {
        String createdBy = extractUserIdFromRequest(httpRequest);
        SaqResponse response = saqService.createSaq(request, createdBy);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response, "SAQ created successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SaqResponse>> getSaqById(@PathVariable String id) {
        SaqResponse response = saqService.getSaqById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "SAQ retrieved successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<SaqResponse>>> getAllSaqs(
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

        Page<SaqResponse> saqPage = saqService.getAllSaqs(pageable, lang, difficultyLevel, tags, isActive);

        PaginatedResponse<SaqResponse> paginatedResponse = PaginatedResponse.of(saqPage);

        return ResponseEntity.ok(ApiResponse.success(paginatedResponse, "SAQs retrieved successfully"));
    }

    @GetMapping("/active/{lang}")
    public ResponseEntity<ApiResponse<List<SaqResponse>>> getActiveSaqsByLang(@PathVariable String lang) {
        try {
            Saq.Language language = Saq.Language.valueOf(lang.toUpperCase());
            List<SaqResponse> responses = saqService.getActiveSaqsByLang(language);
            return ResponseEntity.ok(ApiResponse.success(responses, "SAQs retrieved successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "Invalid language: " + lang, null));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SaqResponse>> updateSaq(
            @PathVariable String id,
            @Valid @RequestBody UpdateSaqRequest request,
            HttpServletRequest httpRequest) {
        String updatedBy = extractUserIdFromRequest(httpRequest);
        SaqResponse response = saqService.updateSaq(id, request, updatedBy);
        return ResponseEntity.ok(ApiResponse.success(response, "SAQ updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSaq(
            @PathVariable String id,
            HttpServletRequest httpRequest) {
        String deletedBy = extractUserIdFromRequest(httpRequest);
        saqService.deleteSaq(id, deletedBy);
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