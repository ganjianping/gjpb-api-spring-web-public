package org.ganjp.blog.rubi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ganjp.blog.rubi.config.RubiProperties;
import org.ganjp.blog.rubi.model.dto.QuestionAnswerImageCreateRequest;
import org.ganjp.blog.rubi.model.dto.QuestionAnswerImageUpdateRequest;
import org.ganjp.blog.rubi.model.dto.QuestionAnswerImageResponse;
import org.ganjp.blog.rubi.model.entity.QuestionAnswerImage;
import org.ganjp.blog.rubi.repository.QuestionAnswerImageRepository;
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
public class QuestionAnswerImageService {
    private final QuestionAnswerImageRepository questionAnswerImageRepository;
    private final RubiProperties rubiProperties;

    public org.springframework.core.io.Resource getImage(String filename) {
        try {
            Path uploadPath = Paths.get(rubiProperties.getQuestionImage().getUpload().getDirectory());
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
        Path uploadPath = Paths.get(rubiProperties.getQuestionImage().getUpload().getDirectory());
        Path filePath = uploadPath.resolve(filename);
        return filePath.toFile();
    }

    public QuestionAnswerImageResponse getQuestionAnswerImageById(String id) {
        Optional<QuestionAnswerImage> imageOpt = questionAnswerImageRepository.findByIdAndIsActiveTrue(id);
        return imageOpt.map(this::toResponse).orElse(null);
    }

    public List<QuestionAnswerImageResponse> listQuestionAnswerImagesByMcq(String mcqId) {
        List<QuestionAnswerImage> images = questionAnswerImageRepository.findByMcqIdAndIsActiveTrueOrderByDisplayOrderAsc(mcqId);
        return images.stream().map(this::toResponse).toList();
    }

    public List<QuestionAnswerImageResponse> listQuestionAnswerImagesBySaq(String saqId) {
        List<QuestionAnswerImage> images = questionAnswerImageRepository.findBySaqIdAndIsActiveTrueOrderByDisplayOrderAsc(saqId);
        return images.stream().map(this::toResponse).toList();
    }

    public QuestionAnswerImageResponse createQuestionAnswerImage(QuestionAnswerImageCreateRequest request, String userId) {
        try {
            if (request.getFilename() == null || request.getFilename().trim().isEmpty()) {
                throw new IllegalArgumentException("Filename is required");
            }

            String targetFilename = request.getFilename();
            String targetExtension = getFileExtension(targetFilename);
            
            BufferedImage bufferedImage;
            String sourceExtension;

            if (request.getFile() != null && !request.getFile().isEmpty()) {
                MultipartFile file = request.getFile();
                String originalFilename = file.getOriginalFilename();
                sourceExtension = getFileExtension(originalFilename);
                bufferedImage = ImageIO.read(file.getInputStream());
            } else if (request.getOriginalUrl() != null && !request.getOriginalUrl().trim().isEmpty()) {
                java.net.URL url = new java.net.URL(request.getOriginalUrl());
                bufferedImage = ImageIO.read(url);
                String path = url.getPath();
                sourceExtension = getFileExtension(path);
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

            Path uploadPath = Paths.get(rubiProperties.getQuestionImage().getUpload().getDirectory());
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

            QuestionAnswerImage questionAnswerImage = QuestionAnswerImage.builder()
                    .id(UUID.randomUUID().toString())
                    .mcqId(request.getMcqId())
                    .saqId(request.getSaqId())
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

            QuestionAnswerImage saved = questionAnswerImageRepository.save(questionAnswerImage);
            return toResponse(saved);
        } catch (IOException e) {
            log.error("Error creating question answer image", e);
            throw new java.io.UncheckedIOException("Failed to store file", e);
        }
    }

    public QuestionAnswerImageResponse updateQuestionAnswerImage(String id, QuestionAnswerImageUpdateRequest request, String userId) {
        Optional<QuestionAnswerImage> imageOpt = questionAnswerImageRepository.findByIdAndIsActiveTrue(id);
        if (imageOpt.isEmpty()) return null;
        QuestionAnswerImage image = imageOpt.get();

        if (request.getMcqId() != null) image.setMcqId(request.getMcqId());
        if (request.getSaqId() != null) image.setSaqId(request.getSaqId());
        if (request.getOriginalUrl() != null) image.setOriginalUrl(request.getOriginalUrl());
        if (request.getLang() != null) image.setLang(request.getLang());
        if (request.getDisplayOrder() != null) image.setDisplayOrder(request.getDisplayOrder());
        if (request.getIsActive() != null) image.setIsActive(request.getIsActive());

        image.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        image.setUpdatedBy(userId);

        QuestionAnswerImage saved = questionAnswerImageRepository.save(image);
        return toResponse(saved);
    }

    public void deleteQuestionAnswerImage(String id) {
        Optional<QuestionAnswerImage> imageOpt = questionAnswerImageRepository.findById(id);
        if (imageOpt.isPresent()) {
            QuestionAnswerImage image = imageOpt.get();
            // Soft delete
            image.setIsActive(false);
            questionAnswerImageRepository.save(image);
        }
    }

    public void deleteQuestionAnswerImagePermanently(String id) {
        Optional<QuestionAnswerImage> imageOpt = questionAnswerImageRepository.findById(id);
        if (imageOpt.isPresent()) {
            QuestionAnswerImage image = imageOpt.get();
            
            // Delete file
            if (image.getFilename() != null) {
                try {
                    Path uploadPath = Paths.get(rubiProperties.getQuestionImage().getUpload().getDirectory());
                    Path filePath = uploadPath.resolve(image.getFilename());
                    Files.deleteIfExists(filePath);
                } catch (IOException e) {
                    log.error("Failed to delete file for question answer image: " + id, e);
                }
            }
            
            questionAnswerImageRepository.delete(image);
        }
    }
    
    public List<QuestionAnswerImageResponse> searchQuestionAnswerImages(
            String mcqId, String saqId, QuestionAnswerImage.Language lang, Boolean isActive) {
        return questionAnswerImageRepository.searchQuestionAnswerImages(mcqId, saqId, lang, isActive)
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

    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return null;
        }
        int lastDot = filename.lastIndexOf('.');
        if (lastDot == -1 || lastDot == filename.length() - 1) {
            return null;
        }
        return filename.substring(lastDot + 1).toLowerCase();
    }

    private QuestionAnswerImageResponse toResponse(QuestionAnswerImage image) {
        String fileUrl = null;
        if (image.getFilename() != null) {
            fileUrl = rubiProperties.getQuestionImage().getBaseUrl() + "/" + image.getFilename();
        }
        return QuestionAnswerImageResponse.builder()
                .id(image.getId())
                .mcqId(image.getMcqId())
                .saqId(image.getSaqId())
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
