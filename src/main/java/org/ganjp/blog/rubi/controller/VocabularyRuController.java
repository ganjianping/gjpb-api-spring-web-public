package org.ganjp.blog.rubi.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ganjp.blog.auth.security.JwtUtils;
import org.ganjp.blog.common.model.ApiResponse;
import org.ganjp.blog.common.model.PaginatedResponse;
import org.ganjp.blog.rubi.model.dto.CreateVocabularyRuRequest;
import org.ganjp.blog.rubi.model.dto.UpdateVocabularyRuRequest;
import org.ganjp.blog.rubi.model.dto.VocabularyRuResponse;
import org.ganjp.blog.rubi.model.entity.VocabularyRu;
import org.ganjp.blog.rubi.service.VocabularyRuService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/vocabulary-rus")
@RequiredArgsConstructor
public class VocabularyRuController {

    private final VocabularyRuService vocabularyService;
    private final JwtUtils jwtUtils;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<VocabularyRuResponse>> createVocabulary(
            @Valid @ModelAttribute CreateVocabularyRuRequest request,
            HttpServletRequest httpRequest) {
        String createdBy = extractUserIdFromRequest(httpRequest);
        VocabularyRuResponse response = vocabularyService.createVocabulary(request, createdBy);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response, "Vocabulary created successfully"));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<VocabularyRuResponse>> createVocabularyJson(
            @Valid @RequestBody CreateVocabularyRuRequest request,
            HttpServletRequest httpRequest) {
        String createdBy = extractUserIdFromRequest(httpRequest);
        VocabularyRuResponse response = vocabularyService.createVocabulary(request, createdBy);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response, "Vocabulary created successfully"));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<VocabularyRuResponse>> updateVocabulary(
            @PathVariable String id,
            @Valid @ModelAttribute UpdateVocabularyRuRequest request,
            HttpServletRequest httpRequest) {
        String updatedBy = extractUserIdFromRequest(httpRequest);
        VocabularyRuResponse response = vocabularyService.updateVocabulary(id, request, updatedBy);
        return ResponseEntity.ok(ApiResponse.success(response, "Vocabulary updated successfully"));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<VocabularyRuResponse>> updateVocabularyJson(
            @PathVariable String id,
            @Valid @RequestBody UpdateVocabularyRuRequest request,
            HttpServletRequest httpRequest) {
        String updatedBy = extractUserIdFromRequest(httpRequest);
        VocabularyRuResponse response = vocabularyService.updateVocabulary(id, request, updatedBy);
        return ResponseEntity.ok(ApiResponse.success(response, "Vocabulary updated successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VocabularyRuResponse>> getVocabularyById(@PathVariable String id) {
        VocabularyRuResponse response = vocabularyService.getVocabularyById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Vocabulary retrieved successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteVocabulary(@PathVariable String id, HttpServletRequest request) {
        String userId = extractUserIdFromRequest(request);
        vocabularyService.deleteVocabulary(id, userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Vocabulary deleted successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<VocabularyRuResponse>>> getVocabularies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "updatedAt") String sort,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String word,
            @RequestParam(required = false) VocabularyRu.Language lang,
            @RequestParam(required = false) String tags,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) Integer term,
            @RequestParam(required = false) Integer week,
            @RequestParam(required = false) String difficultyLevel
    ) {
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, sortDirection, sort);
        
        Page<VocabularyRuResponse> pageResult = vocabularyService.getVocabularies(word, lang, tags, isActive, term, week, difficultyLevel, pageable);
        
        PaginatedResponse<VocabularyRuResponse> response = PaginatedResponse.of(
                pageResult.getContent(), 
                pageResult.getNumber(), 
                pageResult.getSize(), 
                pageResult.getTotalElements()
        );
        
        return ResponseEntity.ok(ApiResponse.success(response, "Vocabularies retrieved successfully"));
    }

    @GetMapping("/by-language/{lang}")
    public ResponseEntity<ApiResponse<List<VocabularyRuResponse>>> getVocabulariesByLanguage(@PathVariable VocabularyRu.Language lang) {
        List<VocabularyRuResponse> response = vocabularyService.getVocabulariesByLanguage(lang);
        return ResponseEntity.ok(ApiResponse.success(response, "Vocabularies retrieved successfully"));
    }

    private String extractUserIdFromRequest(HttpServletRequest request) {
        return jwtUtils.extractUserIdFromToken(request);
    }
}
