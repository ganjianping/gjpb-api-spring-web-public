package org.ganjp.blog.rubi.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ganjp.blog.auth.security.JwtUtils;
import org.ganjp.blog.common.model.ApiResponse;
import org.ganjp.blog.common.model.PaginatedResponse;
import org.ganjp.blog.rubi.model.dto.CreateExpressionRuRequest;
import org.ganjp.blog.rubi.model.dto.UpdateExpressionRuRequest;
import org.ganjp.blog.rubi.model.dto.ExpressionRuResponse;
import org.ganjp.blog.rubi.model.entity.ExpressionRu;
import org.ganjp.blog.rubi.service.ExpressionRuService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/expression-rus")
@RequiredArgsConstructor
public class ExpressionRuController {

    private final ExpressionRuService expressionService;
    private final JwtUtils jwtUtils;

    @PostMapping
    public ResponseEntity<ApiResponse<ExpressionRuResponse>> createExpression(
            @Valid @RequestBody CreateExpressionRuRequest request,
            HttpServletRequest httpRequest) {
        String createdBy = extractUserIdFromRequest(httpRequest);
        ExpressionRuResponse response = expressionService.createExpression(request, createdBy);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response, "Expression created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ExpressionRuResponse>> updateExpression(
            @PathVariable String id,
            @Valid @RequestBody UpdateExpressionRuRequest request,
            HttpServletRequest httpRequest) {
        String updatedBy = extractUserIdFromRequest(httpRequest);
        ExpressionRuResponse response = expressionService.updateExpression(id, request, updatedBy);
        return ResponseEntity.ok(ApiResponse.success(response, "Expression updated successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ExpressionRuResponse>> getExpressionById(@PathVariable String id) {
        ExpressionRuResponse response = expressionService.getExpressionById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Expression retrieved successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteExpression(@PathVariable String id, HttpServletRequest request) {
        String userId = extractUserIdFromRequest(request);
        expressionService.deleteExpression(id, userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Expression deleted successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<ExpressionRuResponse>>> getExpressions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "updatedAt") String sort,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) ExpressionRu.Language lang,
            @RequestParam(required = false) String tags,
            @RequestParam(required = false) Boolean isActive
    ) {
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, sortDirection, sort);
        
        Page<ExpressionRuResponse> pageResult = expressionService.getExpressions(name, lang, tags, isActive, pageable);
        
        PaginatedResponse<ExpressionRuResponse> response = PaginatedResponse.of(
                pageResult.getContent(), 
                pageResult.getNumber(), 
                pageResult.getSize(), 
                pageResult.getTotalElements()
        );
        
        return ResponseEntity.ok(ApiResponse.success(response, "Expressions retrieved successfully"));
    }

    @GetMapping("/by-language/{lang}")
    public ResponseEntity<ApiResponse<List<ExpressionRuResponse>>> getExpressionsByLanguage(@PathVariable ExpressionRu.Language lang) {
        List<ExpressionRuResponse> response = expressionService.getExpressionsByLanguage(lang);
        return ResponseEntity.ok(ApiResponse.success(response, "Expressions retrieved successfully"));
    }

    private String extractUserIdFromRequest(HttpServletRequest request) {
        return jwtUtils.extractUserIdFromToken(request);
    }
}
