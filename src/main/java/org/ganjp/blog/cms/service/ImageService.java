package org.ganjp.blog.cms.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ganjp.blog.cms.config.ImageUploadProperties;
import org.ganjp.blog.cms.model.dto.ImageCreateRequest;
import org.ganjp.blog.cms.model.dto.ImageUpdateRequest;
import org.ganjp.blog.cms.model.dto.ImageResponse;
import org.ganjp.blog.cms.model.entity.Image;
import org.ganjp.blog.cms.repository.ImageRepository;
import org.ganjp.blog.cms.util.CmsUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.File;
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
public class ImageService {
    private final ImageRepository imageRepository;
    private final ImageUploadProperties imageUploadProperties;

    public ImageResponse getImageById(String id) {
        Optional<Image> imageOpt = imageRepository.findByIdAndIsActiveTrue(id);
        return imageOpt.map(this::toResponse).orElse(null);
    }

    public List<ImageResponse> listImages() {
        List<Image> images = imageRepository.findByIsActiveTrueOrderByDisplayOrderAsc();
        return images.stream().map(this::toResponse).toList();
    }

    public ImageResponse updateImage(String id, ImageUpdateRequest request, String userId) {
        Optional<Image> imageOpt = imageRepository.findByIdAndIsActiveTrue(id);
        if (imageOpt.isEmpty()) return null;
        Image image = imageOpt.get();
        if (request.getName() != null) image.setName(request.getName());
        if (request.getOriginalUrl() != null) image.setOriginalUrl(request.getOriginalUrl());
        if (request.getSourceName() != null) image.setSourceName(request.getSourceName());
        if (request.getExtension() != null) image.setExtension(request.getExtension());
        if (request.getMimeType() != null) image.setMimeType(request.getMimeType());
        if (request.getAltText() != null) image.setAltText(request.getAltText());
        if (request.getTags() != null) image.setTags(request.getTags());
        if (request.getLang() != null) image.setLang(request.getLang());
        if (request.getDisplayOrder() != null) image.setDisplayOrder(request.getDisplayOrder());
        image.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        image.setUpdatedBy(userId);
        imageRepository.save(image);
        return toResponse(image);
    }

    public boolean deleteImage(String id, String userId) {
        Optional<Image> imageOpt = imageRepository.findByIdAndIsActiveTrue(id);
        if (imageOpt.isEmpty()) return false;
        Image image = imageOpt.get();
        image.setIsActive(false);
        image.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        image.setUpdatedBy(userId);
        imageRepository.save(image);
        return true;
    }

    public List<ImageResponse> searchImages(String keyword) {
        List<Image> images = imageRepository.searchByNameContaining(keyword);
        return images.stream().map(this::toResponse).toList();
    }

    public ImageResponse createImage(ImageCreateRequest request, String userId) throws IOException {
        String id = UUID.randomUUID().toString();
        BufferedImage originalImage;
        String extension;
        MultipartFile file = request.getFile();
        if (file == null || file.isEmpty()) {
            if (request.getOriginalUrl() == null || request.getOriginalUrl().isBlank()) {
                throw new IllegalArgumentException("originalUrl is required if file is empty");
            }
            java.net.URL url = new java.net.URL(request.getOriginalUrl());
            try (var inputStream = url.openStream()) {
                originalImage = ImageIO.read(inputStream);
                String urlPath = url.getPath();
                int dotIdx = urlPath.lastIndexOf('.');
                extension = (dotIdx > 0 && dotIdx < urlPath.length() - 1) ? urlPath.substring(dotIdx + 1).toLowerCase() : "png";
            }
        } else {
            originalImage = ImageIO.read(file.getInputStream());
            String contentType = file.getContentType();
            if (contentType != null && contentType.contains("jpeg")) {
                extension = "jpg";
            } else if (contentType != null && contentType.contains("png")) {
                extension = "png";
            } else if (contentType != null && contentType.contains("gif")) {
                extension = "gif";
            } else if (contentType != null && contentType.contains("webp")) {
                extension = "webp";
            } else {
                extension = "png";
            }
        }
    BufferedImage resizedImage = resizeImageIfNeeded(originalImage, imageUploadProperties.getResize().getMaxSize());
    BufferedImage thumbnailImage = resizeImageIfNeeded(originalImage, imageUploadProperties.getResize().getThumbnailSize());
    String filename = generateFilename(request.getName(), extension, resizedImage.getWidth(), resizedImage.getHeight());
    String thumbnailFilename = generateFilename(request.getName(), extension, thumbnailImage.getWidth(), thumbnailImage.getHeight());

        Path imagePath = Paths.get(imageUploadProperties.getDirectory(), filename);
        Path thumbPath = Paths.get(imageUploadProperties.getDirectory(), thumbnailFilename);
        ImageIO.write(resizedImage, extension, imagePath.toFile());
        ImageIO.write(thumbnailImage, extension, thumbPath.toFile());

        Image image = new Image();
        image.setId(id);
        image.setName(request.getName());
        image.setOriginalUrl(request.getOriginalUrl());
        image.setSourceName(request.getSourceName());
        image.setFilename(filename);
        image.setThumbnailFilename(thumbnailFilename);
        image.setExtension(extension);
        image.setMimeType(CmsUtil.determineContentType(filename));
        image.setSizeBytes(Files.size(imagePath));
        image.setWidth(resizedImage.getWidth());
        image.setHeight(resizedImage.getHeight());
        image.setAltText(request.getAltText());
        image.setTags(request.getTags());
        image.setLang(request.getLang());
        image.setDisplayOrder(request.getDisplayOrder());
        image.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        image.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        image.setCreatedBy(userId);
        image.setUpdatedBy(userId);
        image.setIsActive(request.getIsActive() == null || request.getIsActive());
        imageRepository.save(image);
        return toResponse(image);
    }

    /**
     * Get image file by filename for viewing in browser
     * @param filename The filename to retrieve
     * @return File object representing the image file
     * @throws IOException if file not found or error reading file
     */
    public java.io.File getImageFileByFilename(String filename) throws IOException {
        // Validate that the filename exists in database for security
        List<Image> images = imageRepository.findAll();
        boolean filenameExists = images.stream()
                .anyMatch(image -> filename.equals(image.getFilename()));

        if (!filenameExists) {
            throw new IllegalArgumentException("Image not found with filename: " + filename);
        }

        return getImageFile(filename);
    }

    private BufferedImage resizeImageIfNeeded(BufferedImage image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();
        if (width <= maxSize && height <= maxSize) return image;
        float scale = Math.min((float) maxSize / width, (float) maxSize / height);
        int newWidth = Math.round(width * scale);
        int newHeight = Math.round(height * scale);
        BufferedImage resized = new BufferedImage(newWidth, newHeight, image.getType());
        resized.getGraphics().drawImage(image, 0, 0, newWidth, newHeight, null);
        return resized;
    }

    private String generateFilename(String name, String extension, int width, int height) {
        String safeName = name.replaceAll("[^a-zA-Z0-9-_]", "_");
        return safeName + "_" + width + "_" + height + "_" + System.currentTimeMillis() + "." + extension;
    }

    private ImageResponse toResponse(Image image) {
        ImageResponse resp = new ImageResponse();
        resp.setId(image.getId());
        resp.setName(image.getName());
        resp.setOriginalUrl(image.getOriginalUrl());
        resp.setSourceName(image.getSourceName());
        resp.setFilename(image.getFilename());
        resp.setThumbnailFilename(image.getThumbnailFilename());
        resp.setExtension(image.getExtension());
        resp.setMimeType(image.getMimeType());
        resp.setSizeBytes(image.getSizeBytes());
        resp.setWidth(image.getWidth());
        resp.setHeight(image.getHeight());
        resp.setAltText(image.getAltText());
        resp.setTags(image.getTags());
        resp.setLang(image.getLang());
        resp.setDisplayOrder(image.getDisplayOrder());
        resp.setCreatedBy(image.getCreatedBy());
        resp.setUpdatedBy(image.getUpdatedBy());
        resp.setIsActive(image.getIsActive());
        resp.setCreatedAt(image.getCreatedAt() != null ? image.getCreatedAt().toString() : null);
        resp.setUpdatedAt(image.getUpdatedAt() != null ? image.getUpdatedAt().toString() : null);
        return resp;
    }

    /**
     * Get image file from storage
     * @param filename The filename to retrieve
     * @return File object representing the image file
     * @throws IOException if file not found or error reading file
     */
    public File getImageFile(String filename) throws IOException {
        Path uploadDir = Paths.get(imageUploadProperties.getDirectory());
        Path fullPath = uploadDir.resolve(filename);

        if (!Files.exists(fullPath)) {
            throw new IOException("Image file not found: " + filename);
        }

        File file = fullPath.toFile();
        if (!file.isFile() || !file.canRead()) {
            throw new IOException("Cannot read image file: " + filename);
        }

        log.debug("Retrieved image file: {}", fullPath);
        return file;
    }
}
