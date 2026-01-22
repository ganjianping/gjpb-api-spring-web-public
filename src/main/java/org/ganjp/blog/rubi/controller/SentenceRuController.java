package org.ganjp.blog.rubi.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ganjp.blog.auth.security.JwtUtils;
import org.ganjp.blog.common.model.ApiResponse;
import org.ganjp.blog.common.model.PaginatedResponse;
import org.ganjp.blog.rubi.model.dto.CreateSentenceRuRequest;
import org.ganjp.blog.rubi.model.dto.UpdateSentenceRuRequest;
import org.ganjp.blog.rubi.model.dto.SentenceRuResponse;
import org.ganjp.blog.rubi.model.entity.SentenceRu;
import org.ganjp.blog.rubi.service.SentenceRuService;
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
@RequestMapping("/v1/sentence-rus")
@RequiredArgsConstructor
public class SentenceRuController {

    private final SentenceRuService sentenceService;
    private final JwtUtils jwtUtils;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<SentenceRuResponse>> createSentence(
            @Valid @ModelAttribute CreateSentenceRuRequest request,
            HttpServletRequest httpRequest) {
        String createdBy = extractUserIdFromRequest(httpRequest);
        SentenceRuResponse response = sentenceService.createSentence(request, createdBy);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response, "Sentence created successfully"));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<SentenceRuResponse>> createSentenceJson(
            @Valid @RequestBody CreateSentenceRuRequest request,
            HttpServletRequest httpRequest) {
        String createdBy = extractUserIdFromRequest(httpRequest);
        SentenceRuResponse response = sentenceService.createSentence(request, createdBy);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response, "Sentence created successfully"));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<SentenceRuResponse>> updateSentence(
            @PathVariable String id,
            @Valid @ModelAttribute UpdateSentenceRuRequest request,
            HttpServletRequest httpRequest) {
        String updatedBy = extractUserIdFromRequest(httpRequest);
        SentenceRuResponse response = sentenceService.updateSentence(id, request, updatedBy);
        return ResponseEntity.ok(ApiResponse.success(response, "Sentence updated successfully"));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<SentenceRuResponse>> updateSentenceJson(
            @PathVariable String id,
            @Valid @RequestBody UpdateSentenceRuRequest request,
            HttpServletRequest httpRequest) {
        String updatedBy = extractUserIdFromRequest(httpRequest);
        SentenceRuResponse response = sentenceService.updateSentence(id, request, updatedBy);
        return ResponseEntity.ok(ApiResponse.success(response, "Sentence updated successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SentenceRuResponse>> getSentenceById(@PathVariable String id) {
        SentenceRuResponse response = sentenceService.getSentenceById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Sentence retrieved successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSentence(@PathVariable String id, HttpServletRequest request) {
        String userId = extractUserIdFromRequest(request);
        sentenceService.deleteSentence(id, userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Sentence deleted successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<SentenceRuResponse>>> getSentences(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "updatedAt") String sort,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) SentenceRu.Language lang,
            @RequestParam(required = false) String tags,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) Integer term,
            @RequestParam(required = false) Integer week,
            @RequestParam(required = false) String difficultyLevel
    ) {
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, sortDirection, sort);
        
        Page<SentenceRuResponse> pageResult = sentenceService.getSentences(name, lang, tags, isActive, term, week, difficultyLevel, pageable);
        
        PaginatedResponse<SentenceRuResponse> response = PaginatedResponse.of(
                pageResult.getContent(), 
                pageResult.getNumber(), 
                pageResult.getSize(), 
                pageResult.getTotalElements()
        );
        
        return ResponseEntity.ok(ApiResponse.success(response, "Sentences retrieved successfully"));
    }

    @GetMapping("/by-language/{lang}")
    public ResponseEntity<ApiResponse<List<SentenceRuResponse>>> getSentencesByLanguage(@PathVariable SentenceRu.Language lang) {
        List<SentenceRuResponse> response = sentenceService.getSentencesByLanguage(lang);
        return ResponseEntity.ok(ApiResponse.success(response, "Sentences retrieved successfully"));
    }

    private String extractUserIdFromRequest(HttpServletRequest request) {
        return jwtUtils.extractUserIdFromToken(request);
    }
}
