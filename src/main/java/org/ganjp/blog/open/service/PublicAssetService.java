package org.ganjp.blog.open.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ganjp.blog.bm.model.entity.AppSetting;
import org.ganjp.blog.bm.repository.AppSettingRepository;
import org.ganjp.blog.cms.repository.LogoRepository;
import org.ganjp.blog.cms.service.ImageService;
import org.ganjp.blog.cms.service.LogoProcessingService;
import org.ganjp.blog.cms.repository.ImageRepository;
import org.ganjp.blog.cms.repository.VideoRepository;
import org.ganjp.blog.open.model.PublicAppSettingDto;
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
public class PublicAssetService {
    private final AppSettingRepository appSettingRepository;
    private final LogoRepository logoRepository;
    private final ImageRepository imageRepository;
    private final ImageService imageService;
    private final LogoProcessingService logoProcessingService;
    private final VideoRepository videoRepository;
    private final org.ganjp.blog.cms.service.VideoService videoService;
    private final org.ganjp.blog.cms.repository.AudioRepository audioRepository;
    private final org.ganjp.blog.cms.service.AudioService audioService;
    private final org.ganjp.blog.cms.repository.ArticleRepository articleRepository;
    private final org.ganjp.blog.cms.service.ArticleService articleService;
    private final org.ganjp.blog.cms.repository.ArticleImageRepository articleImageRepository;
    private final org.ganjp.blog.cms.service.ArticleImageService articleImageService;
    private final org.ganjp.blog.cms.repository.FileRepository fileRepository;
    private final org.ganjp.blog.cms.service.FileService fileService;
    private final org.ganjp.blog.rubi.repository.QuestionImageRuRepository questionImageRuRepository;
    private final org.ganjp.blog.rubi.service.QuestionImageRuService questionImageRuService;
    private final org.ganjp.blog.rubi.repository.VocabularyRuRepository vocabularyRuRepository;
    private final org.ganjp.blog.rubi.service.VocabularyRuService vocabularyRuService;
    private final org.ganjp.blog.rubi.repository.ImageRuRepository imageRuRepository;
    private final org.ganjp.blog.rubi.service.ImageRuService imageRuService;
    private final org.ganjp.blog.rubi.repository.VideoRuRepository videoRuRepository;
    private final org.ganjp.blog.rubi.service.VideoRuService videoRuService;
    private final org.ganjp.blog.rubi.repository.AudioRuRepository audioRuRepository;
    private final org.ganjp.blog.rubi.service.AudioRuService audioRuService;
    private final org.ganjp.blog.rubi.repository.ArticleRuRepository articleRuRepository;
    private final org.ganjp.blog.rubi.service.ArticleRuService articleRuService;
    private final org.ganjp.blog.rubi.repository.ArticleImageRuRepository articleImageRuRepository;
    private final org.ganjp.blog.rubi.service.ArticleImageRuService articleImageRuService;
    /**
     * Get image file by filename for public viewing
     * No authentication required
     * @param filename The filename to retrieve
     * @return File object representing the image file
     * @throws IOException if file not found or error reading file
     */
    public File getImageFile(String filename) throws IOException {
        log.debug("Fetching public image file: {}", filename);
        // Optimize: Query only the image with the given filename and isActive=true
        imageRepository.findByFilenameAndIsActiveTrue(filename)
        .orElseThrow(() -> new IllegalArgumentException("Image not found or not active with filename: " + filename));
        return imageService.getImageFile(filename);
    }

    /**
     * Get all public app settings (only name, value, lang)
     * Only returns settings marked as public (isPublic = true)
     */
    public List<PublicAppSettingDto> getAllAppSettings() {
        log.debug("Fetching all public app settings");
        
        List<AppSetting> settings = appSettingRepository.findByIsPublicTrueOrderByNameAscLangAsc();
        
        return settings.stream()
                .map(PublicAppSettingDto::fromEntity)
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
        // Optimize: Query only the logo with the given filename and isActive=true
    logoRepository.findByFilenameAndIsActiveTrue(filename)
        .orElseThrow(() -> new IllegalArgumentException("Logo not found or not active with filename: " + filename));
        return logoProcessingService.getLogoFile(filename);
    }

    /**
     * Get video file by filename for public viewing
     */
    public File getVideoFile(String filename) throws IOException {
        log.debug("Fetching public video file: {}", filename);
        if (!videoRepository.existsByFilename(filename)) {
            throw new IllegalArgumentException("Video not found or not active with filename: " + filename);
        }
        return videoService.getVideoFileByFilename(filename);
    }

    /**
     * Get video cover image file by filename for public viewing
     */
    public File getVideoCoverFile(String filename) throws IOException {
        log.debug("Fetching public video cover image file: {}", filename);
        // Ensure a Video exists with this cover image filename or main filename
        if (!videoRepository.existsByFilename(filename)) {
            throw new IllegalArgumentException("Video cover image not found or not active with filename: " + filename);
        }
        // Delegate to VideoService to locate the cover image under images/ directory
        return videoService.getCoverImageFileByFilename(filename);
    }

    /**
     * Get audio file by filename for public viewing
     */
    public File getAudioFile(String filename) throws IOException {
        log.debug("Fetching public audio file: {}", filename);
        if (!audioRepository.existsByFilename(filename)) {
            throw new IllegalArgumentException("Audio not found or not active with filename: " + filename);
        }
        return audioService.getAudioFileByFilename(filename);
    }

    /**
     * Get audio cover image file by filename for public viewing
     */
    public File getAudioCoverFile(String filename) throws IOException {
        log.debug("Fetching public audio cover image file: {}", filename);
        if (!audioRepository.existsByFilename(filename)) {
            throw new IllegalArgumentException("Audio cover image not found or not active with filename: " + filename);
        }
        return audioService.getCoverImageFileByFilename(filename);
    }

    /**
     * Get article cover image file by filename for public viewing
     */
    public File getArticleCoverFile(String filename) throws IOException {
        log.debug("Fetching public article cover image file: {}", filename);
        if (!articleRepository.existsByCoverImageFilename(filename)) {
            throw new IllegalArgumentException("Article cover image not found or not active with filename: " + filename);
        }
        return articleService.getCoverImageFileByFilename(filename);
    }

    /**
     * Get article content image file by filename for public viewing
     */
    public File getArticleContentImageFile(String filename) throws IOException {
        log.debug("Fetching public article content image file: {}", filename);
        if (!articleImageRepository.existsByFilename(filename)) {
            throw new IllegalArgumentException("Article content image not found or not active with filename: " + filename);
        }
        return articleImageService.getImageFile(filename);
    }

    /**
     * Get a generic CMS file by filename for public viewing
     * Ensures the file record exists and is active before returning the file
     */
    public File getFile(String filename) throws IOException {
        log.debug("Fetching public cms file: {}", filename);
        if (!fileRepository.existsByFilename(filename)) {
            throw new IllegalArgumentException("File not found or not active with filename: " + filename);
        }
        return fileService.getFileByFilename(filename);
    }

    /**
     * Get question image file by filename for public viewing
     * Ensures the question image record exists and is active before returning the file
     */
    public File getQuestionImageFile(String filename) throws IOException {
        log.debug("Fetching public question image file: {}", filename);
        if (!questionImageRuRepository.existsByFilenameAndIsActiveTrue(filename)) {
            throw new IllegalArgumentException("Question image not found or not active with filename: " + filename);
        }
        return questionImageRuService.getImageFile(filename);
    }

    /**
     * Get vocabulary audio file by filename for public viewing
     * Ensures the vocabulary record exists and is active before returning the file
     */
    public File getVocabularyAudioFile(String filename) throws IOException {
        log.debug("Fetching public vocabulary audio file: {}", filename);
        if (!vocabularyRuRepository.existsByPhoneticAudioFilenameAndIsActiveTrue(filename)) {
            throw new IllegalArgumentException("Vocabulary audio not found or not active with filename: " + filename);
        }
        return vocabularyRuService.getAudioFile(filename);
    }

    /**
     * Get vocabulary image file by filename for public viewing
     * Ensures the vocabulary record exists and is active before returning the file
     */
    public File getVocabularyImageFile(String filename) throws IOException {
        log.debug("Fetching public vocabulary image file: {}", filename);
        if (!vocabularyRuRepository.existsByImageFilenameAndIsActiveTrue(filename)) {
            throw new IllegalArgumentException("Vocabulary image not found or not active with filename: " + filename);
        }
        return vocabularyRuService.getImageFile(filename);
    }

    /**
     * Get Rubi image file by filename for public viewing
     */
    public File getImageRuFile(String filename) throws IOException {
        log.debug("Fetching public Rubi image file: {}", filename);
        imageRuRepository.findByFilenameAndIsActiveTrue(filename)
                .orElseThrow(() -> new IllegalArgumentException("Rubi image not found or not active with filename: " + filename));
        return imageRuService.getImageFile(filename);
    }

    /**
     * Get Rubi video file by filename for public viewing
     */
    public File getVideoRuFile(String filename) throws IOException {
        log.debug("Fetching public Rubi video file: {}", filename);
        videoRuRepository.findByFilenameAndIsActiveTrue(filename)
                .orElseThrow(() -> new IllegalArgumentException("Rubi video not found or not active with filename: " + filename));
        return videoRuService.getVideoFileByFilename(filename);
    }

    /**
     * Get Rubi video cover image file by filename for public viewing
     */
    public File getVideoRuCoverFile(String filename) throws IOException {
        log.debug("Fetching public Rubi video cover image file: {}", filename);
        if (!videoRuRepository.existsByFilename(filename)) {
            throw new IllegalArgumentException("Rubi video cover image not found or not active with filename: " + filename);
        }
        return videoRuService.getCoverImageFileByFilename(filename);
    }

    /**
     * Get Rubi audio file by filename for public viewing
     */
    public File getAudioRuFile(String filename) throws IOException {
        log.debug("Fetching public Rubi audio file: {}", filename);
        audioRuRepository.findByFilenameAndIsActiveTrue(filename)
                .orElseThrow(() -> new IllegalArgumentException("Rubi audio not found or not active with filename: " + filename));
        return audioRuService.getAudioFileByFilename(filename);
    }

    /**
     * Get Rubi audio cover image file by filename for public viewing
     */
    public File getAudioRuCoverFile(String filename) throws IOException {
        log.debug("Fetching public Rubi audio cover image file: {}", filename);
        if (!audioRuRepository.existsByFilename(filename)) {
            throw new IllegalArgumentException("Rubi audio cover image not found or not active with filename: " + filename);
        }
        return audioRuService.getCoverImageFileByFilename(filename);
    }

    /**
     * Get Rubi article cover image file by filename for public viewing
     */
    public File getArticleRuCoverFile(String filename) throws IOException {
        log.debug("Fetching public Rubi article cover image file: {}", filename);
        if (!articleRuRepository.existsByCoverImageFilename(filename)) {
            throw new IllegalArgumentException("Rubi article cover image not found or not active with filename: " + filename);
        }
        return articleRuService.getCoverImageFileByFilename(filename);
    }

    /**
     * Get Rubi article content image file by filename for public viewing
     */
    public File getArticleRuContentImageFile(String filename) throws IOException {
        log.debug("Fetching public Rubi article content image file: {}", filename);
        if (!articleImageRuRepository.existsByFilename(filename)) {
            throw new IllegalArgumentException("Rubi article content image not found or not active with filename: " + filename);
        }
        return articleImageRuService.getImageFile(filename);
    }
}
