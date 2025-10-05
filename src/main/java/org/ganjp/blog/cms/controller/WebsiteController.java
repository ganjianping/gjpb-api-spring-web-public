package org.ganjp.blog.cms.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ganjp.blog.auth.security.JwtUtils;

import org.ganjp.blog.cms.model.dto.CreateWebsiteRequest;
import org.ganjp.blog.cms.model.dto.UpdateWebsiteRequest;
import org.ganjp.blog.cms.model.dto.WebsiteResponse;
import org.ganjp.blog.cms.model.entity.Website;
import org.ganjp.blog.cms.service.WebsiteService;
import org.ganjp.blog.common.model.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Website management
 */
@RestController
@RequestMapping("/v1/websites")
@RequiredArgsConstructor
public class WebsiteController {

    private final WebsiteService websiteService;
    private final JwtUtils jwtUtils;

    /**
     * Get all websites with pagination and filtering
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<WebsiteResponse>>> getWebsites(
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) Website.Language lang,
            @RequestParam(required = false) Boolean isActive,
            Pageable pageable
    ) {
        Page<WebsiteResponse> websites = websiteService.getWebsites(searchTerm, lang, isActive, pageable);
        return ResponseEntity.ok(ApiResponse.success(websites, "Websites retrieved successfully"));
    }

    /**
     * Get website by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WebsiteResponse>> getWebsiteById(@PathVariable String id) {
        WebsiteResponse website = websiteService.getWebsiteById(id);
        return ResponseEntity.ok(ApiResponse.success(website, "Website retrieved successfully"));
    }

    /**
     * Get websites by language
     */
    @GetMapping("/by-language/{lang}")
    public ResponseEntity<ApiResponse<List<WebsiteResponse>>> getWebsitesByLanguage(
            @PathVariable Website.Language lang,
            @RequestParam(defaultValue = "false") boolean activeOnly
    ) {
        List<WebsiteResponse> websites = websiteService.getWebsitesByLanguage(lang, activeOnly);
        return ResponseEntity.ok(ApiResponse.success(websites, "Websites retrieved successfully"));
    }

    /**
     * Get websites by tag
     */
    @GetMapping("/by-tag")
    public ResponseEntity<ApiResponse<List<WebsiteResponse>>> getWebsitesByTag(
            @RequestParam String tag,
            @RequestParam(defaultValue = "true") boolean activeOnly
    ) {
        List<WebsiteResponse> websites = websiteService.getWebsitesByTag(tag, activeOnly);
        return ResponseEntity.ok(ApiResponse.success(websites, "Websites retrieved successfully"));
    }

    /**
     * Get top websites
     */
    @GetMapping("/top")
    public ResponseEntity<ApiResponse<List<WebsiteResponse>>> getTopWebsites(
            @RequestParam(defaultValue = "10") int limit
    ) {
        List<WebsiteResponse> websites = websiteService.getTopWebsites(limit);
        return ResponseEntity.ok(ApiResponse.success(websites, "Top websites retrieved successfully"));
    }

    /**
     * Get website statistics
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<WebsiteService.WebsiteStatistics>> getStatistics() {
        WebsiteService.WebsiteStatistics statistics = websiteService.getStatistics();
        return ResponseEntity.ok(ApiResponse.success(statistics, "Statistics retrieved successfully"));
    }

    /**
     * Create a new website
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_EDITOR')")
    public ResponseEntity<ApiResponse<WebsiteResponse>> createWebsite(
            @Valid @RequestBody CreateWebsiteRequest request,
            HttpServletRequest httpRequest
    ) {
        String createdBy = extractUserIdFromRequest(httpRequest);
        WebsiteResponse website = websiteService.createWebsite(request, createdBy);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(website, "Website created successfully"));
    }

    /**
     * Update an existing website
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_EDITOR')")
    public ResponseEntity<ApiResponse<WebsiteResponse>> updateWebsite(
            @PathVariable String id,
            @Valid @RequestBody UpdateWebsiteRequest request,
            HttpServletRequest httpRequest
    ) {
        String updatedBy = extractUserIdFromRequest(httpRequest);
        WebsiteResponse website = websiteService.updateWebsite(id, request, updatedBy);
        return ResponseEntity.ok(ApiResponse.success(website, "Website updated successfully"));
    }

    /**
     * Delete a website
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteWebsite(@PathVariable String id) {
        websiteService.deleteWebsite(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Website deleted successfully"));
    }

    /**
     * Deactivate a website (soft delete)
     */
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_EDITOR')")
    public ResponseEntity<ApiResponse<WebsiteResponse>> deactivateWebsite(
            @PathVariable String id,
            HttpServletRequest httpRequest
    ) {
        String updatedBy = extractUserIdFromRequest(httpRequest);
        WebsiteResponse website = websiteService.deactivateWebsite(id, updatedBy);
        return ResponseEntity.ok(ApiResponse.success(website, "Website deactivated successfully"));
    }

    /**
     * Activate a website
     */
    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_EDITOR')")
    public ResponseEntity<ApiResponse<WebsiteResponse>> activateWebsite(
            @PathVariable String id,
            HttpServletRequest httpRequest
    ) {
        String updatedBy = extractUserIdFromRequest(httpRequest);
        WebsiteResponse website = websiteService.activateWebsite(id, updatedBy);
        return ResponseEntity.ok(ApiResponse.success(website, "Website activated successfully"));
    }

    /**
     * Bulk activate websites
     */
    @PatchMapping("/bulk/activate")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<String>> bulkActivateWebsites(
            @RequestBody List<String> ids,
            HttpServletRequest httpRequest
    ) {
        String updatedBy = extractUserIdFromRequest(httpRequest);
        int count = 0;
        for (String id : ids) {
            try {
                websiteService.activateWebsite(id, updatedBy);
                count++;
            } catch (Exception e) {
                // Log error but continue with other IDs
            }
        }
        return ResponseEntity.ok(ApiResponse.success(null, String.format("Successfully activated %d websites", count)));
    }

    /**
     * Bulk deactivate websites
     */
    @PatchMapping("/bulk/deactivate")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<String>> bulkDeactivateWebsites(
            @RequestBody List<String> ids,
            HttpServletRequest httpRequest
    ) {
        String updatedBy = extractUserIdFromRequest(httpRequest);
        int count = 0;
        for (String id : ids) {
            try {
                websiteService.deactivateWebsite(id, updatedBy);
                count++;
            } catch (Exception e) {
                // Log error but continue with other IDs
            }
        }
        return ResponseEntity.ok(ApiResponse.success(null, String.format("Successfully deactivated %d websites", count)));
    }

    /**
     * Extract user ID from JWT token in the Authorization header
     * @param request HttpServletRequest containing the Authorization header
     * @return User ID extracted from token
     */
    private String extractUserIdFromRequest(HttpServletRequest request) {
        return jwtUtils.extractUserIdFromToken(request);
    }
}