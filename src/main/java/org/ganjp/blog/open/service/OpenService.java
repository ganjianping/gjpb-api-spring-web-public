package org.ganjp.blog.open.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ganjp.blog.bm.model.entity.AppSetting;
import org.ganjp.blog.bm.repository.AppSettingRepository;
import org.ganjp.blog.open.model.OpenAppSettingDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Open service for accessing app settings without authentication
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OpenService {

    private final AppSettingRepository appSettingRepository;

    /**
     * Get all public app settings (only name, value, lang)
     * Only returns settings marked as public (isPublic = true)
     */
    public List<OpenAppSettingDto> getAllAppSettings() {
        log.debug("Fetching all public app settings");
        
        List<AppSetting> settings = appSettingRepository.findByIsPublicTrueOrderByNameAscLangAsc();
        
        return settings.stream()
                .map(OpenAppSettingDto::fromEntity)
                .collect(Collectors.toList());
    }
}
