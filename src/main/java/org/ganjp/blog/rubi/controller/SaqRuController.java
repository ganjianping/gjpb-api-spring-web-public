package org.ganjp.blog.rubi.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ganjp.blog.auth.security.JwtUtils;
import org.ganjp.blog.common.model.ApiResponse;
import org.ganjp.blog.common.model.PaginatedResponse;
import org.ganjp.blog.rubi.model.dto.CreateSaqRuRequest;
import org.ganjp.blog.rubi.model.dto.SaqRuResponse;
import org.ganjp.blog.rubi.model.dto.UpdateSaqRuRequest;
import org.ganjp.blog.rubi.model.entity.SaqRu;
import org.ganjp.blog.rubi.service.SaqRuService;
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
public class SaqRuController {

    private final SaqRuService saqRuService;
    private final JwtUtils jwtUtils;

    @PostMapping
    public ResponseEntity<ApiResponse<SaqRuResponse>> createSaq(
            @Valid @RequestBody CreateSaqRuRequest request,
            HttpServletRequest httpRequest) {
        String createdBy = extractUserIdFromRequest(httpRequest);
        SaqRuResponse response = saqRuService.createSaq(request, createdBy);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response, "SAQ created successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SaqRuResponse>> getSaqById(@PathVariable String id) {
        SaqRuResponse response = saqRuService.getSaqById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "SAQ retrieved successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<SaqRuResponse>>> getAllSaqs(
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

        Page<SaqRuResponse> saqPage = saqRuService.getAllSaqs(pageable, lang, difficultyLevel, tags, isActive);

        PaginatedResponse<SaqRuResponse> paginatedResponse = PaginatedResponse.of(saqPage);

        return ResponseEntity.ok(ApiResponse.success(paginatedResponse, "SAQs retrieved successfully"));
    }

    @GetMapping("/active/{lang}")
    public ResponseEntity<ApiResponse<List<SaqRuResponse>>> getActiveSaqsByLang(@PathVariable String lang) {
        try {
            SaqRu.Language language = SaqRu.Language.valueOf(lang.toUpperCase());
            List<SaqRuResponse> responses = saqRuService.getActiveSaqsByLang(language);
            return ResponseEntity.ok(ApiResponse.success(responses, "SAQs retrieved successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "Invalid language: " + lang, null));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SaqRuResponse>> updateSaq(
            @PathVariable String id,
            @Valid @RequestBody UpdateSaqRuRequest request,
            HttpServletRequest httpRequest) {
        String updatedBy = extractUserIdFromRequest(httpRequest);
        SaqRuResponse response = saqRuService.updateSaq(id, request, updatedBy);
        return ResponseEntity.ok(ApiResponse.success(response, "SAQ updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSaq(
            @PathVariable String id,
            HttpServletRequest httpRequest) {
        String deletedBy = extractUserIdFromRequest(httpRequest);
        saqRuService.deleteSaq(id, deletedBy);
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