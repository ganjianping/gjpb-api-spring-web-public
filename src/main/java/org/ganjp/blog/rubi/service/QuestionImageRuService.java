package org.ganjp.blog.rubi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ganjp.blog.rubi.config.RubiProperties;
import org.ganjp.blog.rubi.model.dto.QuestionImageRuCreateRequest;
import org.ganjp.blog.rubi.model.dto.QuestionImageRuUpdateRequest;
import org.ganjp.blog.rubi.model.dto.QuestionImageRuResponse;
import org.ganjp.blog.rubi.model.entity.QuestionImageRu;
import org.ganjp.blog.rubi.repository.QuestionImageRuRepository;
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
public class QuestionImageRuService {
    private final QuestionImageRuRepository questionImageRuRepository;
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

    public QuestionImageRuResponse getQuestionImageRuById(String id) {
        Optional<QuestionImageRu> imageOpt = questionImageRuRepository.findByIdAndIsActiveTrue(id);
        return imageOpt.map(this::toResponse).orElse(null);
    }

    public List<QuestionImageRuResponse> listQuestionImageRusByMultipleChoiceQuestionRu(String multipleChoiceQuestionId) {
        List<QuestionImageRu> images = questionImageRuRepository.findByMultipleChoiceQuestionIdAndIsActiveTrueOrderByDisplayOrderAsc(multipleChoiceQuestionId);
        return images.stream().map(this::toResponse).toList();
    }

    public List<QuestionImageRuResponse> listQuestionImageRusByFreeTextQuestionRu(String freeTextQuestionId) {
        List<QuestionImageRu> images = questionImageRuRepository.findByFreeTextQuestionIdAndIsActiveTrueOrderByDisplayOrderAsc(freeTextQuestionId);
        return images.stream().map(this::toResponse).toList();
    }

    public List<QuestionImageRuResponse> listQuestionImageRusByTrueFalseQuestionRu(String trueFalseQuestionId) {
        List<QuestionImageRu> images = questionImageRuRepository.findByTrueFalseQuestionIdAndIsActiveTrueOrderByDisplayOrderAsc(trueFalseQuestionId);
        return images.stream().map(this::toResponse).toList();
    }

    public List<QuestionImageRuResponse> listQuestionImageRusByFillBlankQuestionRu(String fillBlankQuestionId) {
        List<QuestionImageRu> images = questionImageRuRepository.findByFillBlankQuestionIdAndIsActiveTrueOrderByDisplayOrderAsc(fillBlankQuestionId);
        return images.stream().map(this::toResponse).toList();
    }

    public QuestionImageRuResponse createQuestionImageRu(QuestionImageRuCreateRequest request, String userId) {
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

            QuestionImageRu questionAnswerImage = QuestionImageRu.builder()
                    .id(UUID.randomUUID().toString())
                    .multipleChoiceQuestionId(request.getMultipleChoiceQuestionId())
                    .freeTextQuestionId(request.getFreeTextQuestionId())
                    .trueFalseQuestionId(request.getTrueFalseQuestionId())
                    .fillBlankQuestionId(request.getFillBlankQuestionId())
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

            QuestionImageRu saved = questionImageRuRepository.save(questionAnswerImage);
            return toResponse(saved);
        } catch (IOException e) {
            log.error("Error creating question answer image", e);
            throw new java.io.UncheckedIOException("Failed to store file", e);
        }
    }

    public QuestionImageRuResponse updateQuestionImageRu(String id, QuestionImageRuUpdateRequest request, String userId) {
        Optional<QuestionImageRu> imageOpt = questionImageRuRepository.findByIdAndIsActiveTrue(id);
        if (imageOpt.isEmpty()) return null;
        QuestionImageRu image = imageOpt.get();

        if (request.getMultipleChoiceQuestionId() != null) image.setMultipleChoiceQuestionId(request.getMultipleChoiceQuestionId());
        if (request.getFreeTextQuestionId() != null) image.setFreeTextQuestionId(request.getFreeTextQuestionId());
        if (request.getTrueFalseQuestionId() != null) image.setTrueFalseQuestionId(request.getTrueFalseQuestionId());
        if (request.getFillBlankQuestionId() != null) image.setFillBlankQuestionId(request.getFillBlankQuestionId());
        if (request.getOriginalUrl() != null) image.setOriginalUrl(request.getOriginalUrl());
        if (request.getLang() != null) image.setLang(request.getLang());
        if (request.getDisplayOrder() != null) image.setDisplayOrder(request.getDisplayOrder());
        if (request.getIsActive() != null) image.setIsActive(request.getIsActive());

        image.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        image.setUpdatedBy(userId);

        QuestionImageRu saved = questionImageRuRepository.save(image);
        return toResponse(saved);
    }

    public void deleteQuestionImageRu(String id) {
        Optional<QuestionImageRu> imageOpt = questionImageRuRepository.findById(id);
        if (imageOpt.isPresent()) {
            QuestionImageRu image = imageOpt.get();
            // Soft delete
            image.setIsActive(false);
            questionImageRuRepository.save(image);
        }
    }

    public void deleteQuestionImageRuPermanently(String id) {
        Optional<QuestionImageRu> imageOpt = questionImageRuRepository.findById(id);
        if (imageOpt.isPresent()) {
            QuestionImageRu image = imageOpt.get();
            
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
            
            questionImageRuRepository.delete(image);
        }
    }
    
    public List<QuestionImageRuResponse> searchQuestionImageRus(
            String multipleChoiceQuestionId, String freeTextQuestionId, String trueFalseQuestionId, 
            String fillBlankQuestionId, QuestionImageRu.Language lang, Boolean isActive) {
        return questionImageRuRepository.searchQuestionImageRus(multipleChoiceQuestionId, freeTextQuestionId, 
                trueFalseQuestionId, fillBlankQuestionId, lang, isActive)
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

    private QuestionImageRuResponse toResponse(QuestionImageRu image) {
        String fileUrl = null;
        if (image.getFilename() != null) {
            fileUrl = rubiProperties.getQuestionImage().getBaseUrl() + "/" + image.getFilename();
        }
        return QuestionImageRuResponse.builder()
                .id(image.getId())
                .multipleChoiceQuestionId(image.getMultipleChoiceQuestionId())
                .freeTextQuestionId(image.getFreeTextQuestionId())
                .trueFalseQuestionId(image.getTrueFalseQuestionId())
                .fillBlankQuestionId(image.getFillBlankQuestionId())
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
