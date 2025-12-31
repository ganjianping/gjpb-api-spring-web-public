package org.ganjp.blog.rubi.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ganjp.blog.auth.security.JwtUtils;
import org.ganjp.blog.common.model.ApiResponse;
import org.ganjp.blog.common.model.PaginatedResponse;
import org.ganjp.blog.rubi.model.dto.CreateVocabularyRequest;
import org.ganjp.blog.rubi.model.dto.UpdateVocabularyRequest;
import org.ganjp.blog.rubi.model.dto.VocabularyResponse;
import org.ganjp.blog.rubi.model.entity.Vocabulary;
import org.ganjp.blog.rubi.service.VocabularyService;
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
@RequestMapping("/v1/vocabularies")
@RequiredArgsConstructor
public class VocabularyController {

    private final VocabularyService vocabularyService;
    private final JwtUtils jwtUtils;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<VocabularyResponse>> createVocabulary(
            @Valid @ModelAttribute CreateVocabularyRequest request,
            HttpServletRequest httpRequest) {
        String createdBy = extractUserIdFromRequest(httpRequest);
        VocabularyResponse response = vocabularyService.createVocabulary(request, createdBy);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response, "Vocabulary created successfully"));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<VocabularyResponse>> createVocabularyJson(
            @Valid @RequestBody CreateVocabularyRequest request,
            HttpServletRequest httpRequest) {
        String createdBy = extractUserIdFromRequest(httpRequest);
        VocabularyResponse response = vocabularyService.createVocabulary(request, createdBy);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response, "Vocabulary created successfully"));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<VocabularyResponse>> updateVocabulary(
            @PathVariable String id,
            @Valid @ModelAttribute UpdateVocabularyRequest request,
            HttpServletRequest httpRequest) {
        String updatedBy = extractUserIdFromRequest(httpRequest);
        VocabularyResponse response = vocabularyService.updateVocabulary(id, request, updatedBy);
        return ResponseEntity.ok(ApiResponse.success(response, "Vocabulary updated successfully"));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<VocabularyResponse>> updateVocabularyJson(
            @PathVariable String id,
            @Valid @RequestBody UpdateVocabularyRequest request,
            HttpServletRequest httpRequest) {
        String updatedBy = extractUserIdFromRequest(httpRequest);
        VocabularyResponse response = vocabularyService.updateVocabulary(id, request, updatedBy);
        return ResponseEntity.ok(ApiResponse.success(response, "Vocabulary updated successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VocabularyResponse>> getVocabularyById(@PathVariable String id) {
        VocabularyResponse response = vocabularyService.getVocabularyById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Vocabulary retrieved successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteVocabulary(@PathVariable String id, HttpServletRequest request) {
        String userId = extractUserIdFromRequest(request);
        vocabularyService.deleteVocabulary(id, userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Vocabulary deleted successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<VocabularyResponse>>> getVocabularies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "updatedAt") String sort,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String word,
            @RequestParam(required = false) Vocabulary.Language lang,
            @RequestParam(required = false) String tags,
            @RequestParam(required = false) Boolean isActive
    ) {
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, sortDirection, sort);
        
        Page<VocabularyResponse> pageResult = vocabularyService.getVocabularies(word, lang, tags, isActive, pageable);
        
        PaginatedResponse<VocabularyResponse> response = PaginatedResponse.of(
                pageResult.getContent(), 
                pageResult.getNumber(), 
                pageResult.getSize(), 
                pageResult.getTotalElements()
        );
        
        return ResponseEntity.ok(ApiResponse.success(response, "Vocabularies retrieved successfully"));
    }

    @GetMapping("/by-language/{lang}")
    public ResponseEntity<ApiResponse<List<VocabularyResponse>>> getVocabulariesByLanguage(@PathVariable Vocabulary.Language lang) {
        List<VocabularyResponse> response = vocabularyService.getVocabulariesByLanguage(lang);
        return ResponseEntity.ok(ApiResponse.success(response, "Vocabularies retrieved successfully"));
    }

    private String extractUserIdFromRequest(HttpServletRequest request) {
        return jwtUtils.extractUserIdFromToken(request);
    }
}
