package org.ganjp.blog.open.controller;

import lombok.RequiredArgsConstructor;
import org.ganjp.blog.common.model.ApiResponse;
import org.ganjp.blog.open.model.OpenAppSettingDto;
import org.ganjp.blog.open.service.OpenService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Open REST Controller for accessing data without authentication
 * These endpoints are publicly accessible and don't require JWT tokens
 */
@RestController
@RequestMapping("/v1/open")
@RequiredArgsConstructor
public class OpenController {

    private final OpenService openService;

    /**
     * Get all public app settings (only name, value, lang)
     * No authentication required
     * Only returns settings marked as public
     */
    @GetMapping("/app-settings")
    public ResponseEntity<ApiResponse<List<OpenAppSettingDto>>> getAllAppSettings() {
        List<OpenAppSettingDto> settings = openService.getAllAppSettings();
        return ResponseEntity.ok(ApiResponse.success(settings, "Public app settings retrieved successfully"));
    }
}
