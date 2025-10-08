package org.ganjp.blog.cms.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ganjp.blog.cms.model.dto.LogoCreateRequest;
import org.ganjp.blog.cms.model.dto.LogoResponse;
import org.ganjp.blog.cms.model.dto.LogoUpdateRequest;
import org.ganjp.blog.cms.model.entity.Logo;
import org.ganjp.blog.cms.repository.LogoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing logos
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LogoService {

    private final LogoRepository logoRepository;
    private final ImageProcessingService imageProcessingService;

    /**
     * Create a new logo
     */
    @Transactional
    public LogoResponse createLogo(LogoCreateRequest request, String userId) throws IOException {
        log.info("Creating new logo: {}", request.getName());

        // Validate image source
        if (!request.hasImageSource()) {
            throw new IllegalArgumentException("Either file upload or original URL must be provided");
        }

        // Process image (upload or download from URL)
        ImageProcessingService.ProcessedImage processedImage;
        if (request.getFile() != null && !request.getFile().isEmpty()) {
            processedImage = imageProcessingService.processUploadedFile(request.getFile(), request.getName());
        } else {
            processedImage = imageProcessingService.processImageFromUrl(request.getOriginalUrl(), request.getName());
        }

        // Create logo entity
        Logo logo = Logo.builder()
                .id(UUID.randomUUID().toString())
                .name(request.getName())
                .originalUrl(request.getOriginalUrl())
                .filename(processedImage.getFilename())
                .extension(processedImage.getExtension())
                .tags(request.getTags())
                .lang(request.getLang())
                .displayOrder(request.getDisplayOrder())
                .isActive(request.getIsActive())
                .build();

        logo.setCreatedBy(userId);
        logo.setUpdatedBy(userId);

        Logo savedLogo = logoRepository.save(logo);
        log.info("Logo created successfully with ID: {}", savedLogo.getId());

        return toResponse(savedLogo);
    }

    /**
     * Update an existing logo
     */
    @Transactional
    public LogoResponse updateLogo(String id, LogoUpdateRequest request, String userId) throws IOException {
        log.info("Updating logo: {}", id);

        Logo logo = logoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Logo not found with ID: " + id));

        String oldFilename = logo.getFilename();
        boolean imageUpdated = false;
        boolean nameChanged = false;

        // Check if name is being changed
        if (request.getName() != null && !request.getName().equals(logo.getName())) {
            nameChanged = true;
        }

        // Update image if new one provided via URL
        if (request.hasNewImage()) {
            ImageProcessingService.ProcessedImage processedImage;
            
            // Use updated name if provided, otherwise use existing name
            String nameForFilename = request.getName() != null ? request.getName() : logo.getName();
            
            processedImage = imageProcessingService.processImageFromUrl(request.getOriginalUrl(), nameForFilename);

            logo.setOriginalUrl(request.getOriginalUrl());
            logo.setFilename(processedImage.getFilename());
            logo.setExtension(processedImage.getExtension());
            
            imageUpdated = true;
        } else if (nameChanged && !imageUpdated) {
            // If only name changed (no new image), rename the existing file
            String newFilename = imageProcessingService.renameLogoFile(oldFilename, request.getName(), logo.getExtension());
            if (newFilename != null) {
                logo.setFilename(newFilename);
            }
        }

        // Update other fields if provided
        if (request.getName() != null) {
            logo.setName(request.getName());
        }
        if (request.getTags() != null) {
            logo.setTags(request.getTags());
        }
        if (request.getLang() != null) {
            logo.setLang(request.getLang());
        }
        if (request.getDisplayOrder() != null) {
            logo.setDisplayOrder(request.getDisplayOrder());
        }
        if (request.getIsActive() != null) {
            logo.setIsActive(request.getIsActive());
        }

        logo.setUpdatedBy(userId);

        Logo updatedLogo = logoRepository.save(logo);
        
        // Delete old image file if image was updated (replaced with new image)
        if (imageUpdated && oldFilename != null) {
            imageProcessingService.deleteLogoFile(oldFilename);
        }

        log.info("Logo updated successfully: {}", id);
        return toResponse(updatedLogo);
    }

    /**
     * Get logo by ID
     */
    public LogoResponse getLogoById(String id) {
        Logo logo = logoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Logo not found with ID: " + id));
        return toResponse(logo);
    }

    /**
     * Get logo file by filename for viewing in browser
     * @param filename The filename to retrieve
     * @return File object representing the logo file
     * @throws IOException if file not found or error reading file
     */
    public java.io.File getLogoFileByFilename(String filename) throws IOException {
        // Validate that the filename exists in database for security
        List<Logo> logos = logoRepository.findAll();
        boolean filenameExists = logos.stream()
                .anyMatch(logo -> filename.equals(logo.getFilename()));
        
        if (!filenameExists) {
            throw new IllegalArgumentException("Logo not found with filename: " + filename);
        }
        
        return imageProcessingService.getLogoFile(filename);
    }

    /**
     * Get all active logos
     */
    public List<LogoResponse> getAllActiveLogos() {
        return logoRepository.findByIsActiveTrueOrderByDisplayOrderAsc()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all logos (including inactive)
     */
    public List<LogoResponse> getAllLogos() {
        return logoRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Search logos by name
     */
    public List<LogoResponse> searchLogosByName(String keyword) {
        return logoRepository.searchByNameContaining(keyword)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Find logos by tag
     */
    public List<LogoResponse> findLogosByTag(String tag) {
        return logoRepository.findByTagsContaining(tag)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Soft delete logo
     */
    @Transactional
    public void deleteLogo(String id, String userId) {
        Logo logo = logoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Logo not found with ID: " + id));

        logo.setIsActive(false);
        logo.setUpdatedBy(userId);
        logoRepository.save(logo);

        log.info("Logo soft deleted: {}", id);
    }

    /**
     * Permanently delete logo (hard delete)
     */
    @Transactional
    public void permanentlyDeleteLogo(String id) {
        Logo logo = logoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Logo not found with ID: " + id));

        String filename = logo.getFilename();
        
        logoRepository.delete(logo);
        
        // Delete physical file
        if (filename != null) {
            imageProcessingService.deleteLogoFile(filename);
        }

        log.info("Logo permanently deleted: {}", id);
    }

    /**
     * Convert Logo entity to LogoResponse DTO
     */
    private LogoResponse toResponse(Logo logo) {
        return LogoResponse.builder()
                .id(logo.getId())
                .name(logo.getName())
                .originalUrl(logo.getOriginalUrl())
                .filename(logo.getFilename())
                .extension(logo.getExtension())
                .tags(logo.getTags())
                .lang(logo.getLang())
                .displayOrder(logo.getDisplayOrder())
                .isActive(logo.getIsActive())
                .createdAt(logo.getCreatedAt())
                .updatedAt(logo.getUpdatedAt())
                .createdBy(logo.getCreatedBy())
                .updatedBy(logo.getUpdatedBy())
                .build();
    }
}
