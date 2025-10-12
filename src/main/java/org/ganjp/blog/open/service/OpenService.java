package org.ganjp.blog.open.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ganjp.blog.bm.model.entity.AppSetting;
import org.ganjp.blog.bm.repository.AppSettingRepository;
import org.ganjp.blog.cms.model.entity.Logo;
import org.ganjp.blog.cms.repository.LogoRepository;
import org.ganjp.blog.cms.service.LogoProcessingService;
import org.ganjp.blog.open.model.OpenAppSettingDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
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
    private final LogoRepository logoRepository;
    private final LogoProcessingService logoProcessingService;

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

    /**
     * Get logo file by filename for public viewing
     * No authentication required
     * @param filename The filename to retrieve
     * @return File object representing the logo file
     * @throws IOException if file not found or error reading file
     */
    public File getLogoFile(String filename) throws IOException {
        log.debug("Fetching public logo file: {}", filename);
        
        // Validate that the filename exists in database and is active for security
        List<Logo> logos = logoRepository.findByIsActiveTrueOrderByDisplayOrderAsc();
        boolean filenameExists = logos.stream()
                .anyMatch(logo -> filename.equals(logo.getFilename()));
        
        if (!filenameExists) {
            throw new IllegalArgumentException("Logo not found or not active with filename: " + filename);
        }
        
        return logoProcessingService.getLogoFile(filename);
    }
}
