package org.ganjp.blog.rubi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ganjp.blog.rubi.config.ArticleRuProperties;
import org.ganjp.blog.rubi.model.dto.ArticleImageRuCreateRequest;
import org.ganjp.blog.rubi.model.dto.ArticleImageRuUpdateRequest;
import org.ganjp.blog.rubi.model.dto.ArticleImageRuResponse;
import org.ganjp.blog.rubi.model.entity.ArticleImageRu;
import org.ganjp.blog.rubi.repository.ArticleImageRuRepository;
import org.ganjp.blog.rubi.util.RubiUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.imageio.ImageIO;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArticleImageRuService {
    private final ArticleImageRuRepository articleImageRuRepository;
    private final ArticleRuProperties articleProperties;

    public org.springframework.core.io.Resource getImage(String filename) {
        try {
            Path uploadPath = Paths.get(articleProperties.getContentImage().getUpload().getDirectory());
            Path filePath = uploadPath.resolve(filename);
            org.springframework.core.io.Resource resource = new org.springframework.core.io.UrlResource(filePath.toUri());
            
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Could not read the file!");
            }
        } catch (java.net.MalformedURLException e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }

    public java.io.File getImageFile(String filename) {
        Path uploadPath = Paths.get(articleProperties.getContentImage().getUpload().getDirectory());
        Path filePath = uploadPath.resolve(filename);
        return filePath.toFile();
    }

    public ArticleImageRuResponse getArticleImageById(String id) {
        Optional<ArticleImageRu> imageOpt = articleImageRuRepository.findByIdAndIsActiveTrue(id);
        return imageOpt.map(this::toResponse).orElse(null);
    }

    public List<ArticleImageRuResponse> listArticleImages(String articleRuId) {
        List<ArticleImageRu> images = articleImageRuRepository.findByArticleRuIdAndIsActiveTrueOrderByDisplayOrderAsc(articleRuId);
        return images.stream().map(this::toResponse).toList();
    }

    public ArticleImageRuResponse createArticleImage(ArticleImageRuCreateRequest request, String userId) {
        try {
            if (request.getFilename() == null || request.getFilename().trim().isEmpty()) {
                throw new IllegalArgumentException("Filename is required");
            }

            String targetFilename = request.getFilename();
            String targetExtension = RubiUtil.getFileExtension(targetFilename);
            
            BufferedImage bufferedImage;
            String sourceExtension;

            if (request.getFile() != null && !request.getFile().isEmpty()) {
                MultipartFile file = request.getFile();
                String originalFilename = file.getOriginalFilename();
                sourceExtension = RubiUtil.getFileExtension(originalFilename);
                bufferedImage = ImageIO.read(file.getInputStream());
            } else if (request.getOriginalUrl() != null && !request.getOriginalUrl().trim().isEmpty()) {
                java.net.URL url = new java.net.URL(request.getOriginalUrl());
                bufferedImage = ImageIO.read(url);
                String path = url.getPath();
                sourceExtension = RubiUtil.getFileExtension(path);
            } else {
                 throw new IllegalArgumentException("File or Original URL is required");
            }

            if (bufferedImage == null) {
                 throw new IllegalArgumentException("Failed to read image data");
            }

            String finalExtension;
            if (targetExtension != null && !targetExtension.isEmpty()) {
                finalExtension = targetExtension;
            } else {
                if (sourceExtension != null && !sourceExtension.isEmpty()) {
                    finalExtension = sourceExtension;
                } else {
                    finalExtension = "png";
                }
                targetFilename = targetFilename + "." + finalExtension;
            }

            Path uploadPath = Paths.get(articleProperties.getContentImage().getUpload().getDirectory());
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(targetFilename);
            if (Files.exists(filePath)) {
                throw new IllegalArgumentException("File with name " + targetFilename + " already exists");
            }
            ImageIO.write(bufferedImage, finalExtension, filePath.toFile());
            
            Integer width = bufferedImage.getWidth();
            Integer height = bufferedImage.getHeight();

            ArticleImageRu articleImage = ArticleImageRu.builder()
                    .id(UUID.randomUUID().toString())
                    .articleRuId(request.getArticleRuId())
                    .articleRuTitle(request.getArticleRuTitle())
                    .filename(targetFilename)
                    .originalUrl(request.getOriginalUrl())
                    .width(width)
                    .height(height)
                    .lang(request.getLang())
                    .displayOrder(request.getDisplayOrder())
                    .isActive(request.getIsActive())
                    .createdAt(new Timestamp(System.currentTimeMillis()))
                    .updatedAt(new Timestamp(System.currentTimeMillis()))
                    .createdBy(userId)
                    .updatedBy(userId)
                    .build();

            ArticleImageRu saved = articleImageRuRepository.save(articleImage);
            return toResponse(saved);
        } catch (IOException e) {
            log.error("Error creating article image", e);
            throw new java.io.UncheckedIOException("Failed to store file", e);
        }
    }

    public ArticleImageRuResponse updateArticleImage(String id, ArticleImageRuUpdateRequest request, String userId) {
        Optional<ArticleImageRu> imageOpt = articleImageRuRepository.findByIdAndIsActiveTrue(id);
        if (imageOpt.isEmpty()) return null;
        ArticleImageRu image = imageOpt.get();

        if (request.getArticleRuId() != null) image.setArticleRuId(request.getArticleRuId());
        if (request.getArticleRuTitle() != null) image.setArticleRuTitle(request.getArticleRuTitle());
        if (request.getOriginalUrl() != null) image.setOriginalUrl(request.getOriginalUrl());
        if (request.getLang() != null) image.setLang(request.getLang());
        if (request.getDisplayOrder() != null) image.setDisplayOrder(request.getDisplayOrder());
        if (request.getIsActive() != null) image.setIsActive(request.getIsActive());

        image.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        image.setUpdatedBy(userId);

        ArticleImageRu saved = articleImageRuRepository.save(image);
        return toResponse(saved);
    }

    public void deleteArticleImage(String id) {
        Optional<ArticleImageRu> imageOpt = articleImageRuRepository.findById(id);
        if (imageOpt.isPresent()) {
            ArticleImageRu image = imageOpt.get();
            // Soft delete
            image.setIsActive(false);
            articleImageRuRepository.save(image);
        }
    }

    public void deleteArticleImagePermanently(String id) {
        Optional<ArticleImageRu> imageOpt = articleImageRuRepository.findById(id);
        if (imageOpt.isPresent()) {
            ArticleImageRu image = imageOpt.get();
            
            // Delete file
            if (image.getFilename() != null) {
                try {
                    Path uploadPath = Paths.get(articleProperties.getContentImage().getUpload().getDirectory());
                    Path filePath = uploadPath.resolve(image.getFilename());
                    Files.deleteIfExists(filePath);
                } catch (IOException e) {
                    log.error("Failed to delete file for article image: " + id, e);
                }
            }
            
            articleImageRuRepository.delete(image);
        }
    }
    
    public List<ArticleImageRuResponse> searchArticleImages(String articleRuId, ArticleImageRu.Language lang, Boolean isActive) {
        return articleImageRuRepository.searchArticleImages(articleRuId, lang, isActive)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private int[] getImageDimensions(Path filePath) {
        try {
            BufferedImage img = ImageIO.read(filePath.toFile());
            if (img != null) {
                return new int[]{img.getWidth(), img.getHeight()};
            }
        } catch (Exception e) {
            log.warn("Could not read image dimensions for {}", filePath);
        }
        return new int[0];
    }

    private ArticleImageRuResponse toResponse(ArticleImageRu image) {
        String fileUrl = null;
        if (image.getFilename() != null) {
            fileUrl = articleProperties.getContentImage().getBaseUrl() + "/" + image.getFilename();
        }
        return ArticleImageRuResponse.builder()
                .id(image.getId())
                .articleRuId(image.getArticleRuId())
                .articleRuTitle(image.getArticleRuTitle())
                .filename(image.getFilename())
                .fileUrl(fileUrl)
                .originalUrl(image.getOriginalUrl())
                .width(image.getWidth())
                .height(image.getHeight())
                .lang(image.getLang())
                .displayOrder(image.getDisplayOrder())
                .createdBy(image.getCreatedBy())
                .updatedBy(image.getUpdatedBy())
                .isActive(image.getIsActive())
                .createdAt(image.getCreatedAt().toString())
                .updatedAt(image.getUpdatedAt().toString())
                .build();
    }
}
