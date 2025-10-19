package org.ganjp.blog.cms.service;

import lombok.RequiredArgsConstructor;
import org.ganjp.blog.cms.config.VideoUploadProperties;
import org.ganjp.blog.cms.model.dto.VideoCreateRequest;
import org.ganjp.blog.cms.model.dto.VideoResponse;
import org.ganjp.blog.cms.model.dto.VideoUpdateRequest;
import org.ganjp.blog.cms.model.entity.Video;
import org.ganjp.blog.cms.repository.VideoRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
 

@Service
@RequiredArgsConstructor
public class VideoService {
    private final VideoRepository videoRepository;
    private final VideoUploadProperties uploadProperties;

    public VideoResponse createVideo(VideoCreateRequest request, String userId) throws IOException {
        Video video = new Video();
        String id = UUID.randomUUID().toString();
        video.setId(id);
        video.setName(request.getName());
        // originalUrl, sourceName, width, height, duration removed
        video.setCoverImageFilename(request.getCoverImageFilename());
        video.setDescription(request.getDescription());
        video.setTags(request.getTags());
        if (request.getLang() != null) video.setLang(request.getLang());
        if (request.getDisplayOrder() != null) video.setDisplayOrder(request.getDisplayOrder());
        if (request.getIsActive() != null) video.setIsActive(request.getIsActive());

        // handle file upload (required)
        if (request.getFile() != null && !request.getFile().isEmpty()) {
            MultipartFile file = request.getFile();
            String originalFilename = file.getOriginalFilename();
            // prefer original filename; if missing, fall back to timestamp-based name
            String filename;
            if (originalFilename == null || originalFilename.isBlank()) {
                filename = System.currentTimeMillis() + "-video";
            } else {
                filename = originalFilename.replaceAll("\\s+", "-");
            }
            Path videoDir = Path.of(uploadProperties.getDirectory());
            Files.createDirectories(videoDir);
            Path target = videoDir.resolve(filename);

            // If filename already exists on disk or in DB, reject to avoid overwrite
            if (Files.exists(target) || videoRepository.existsByFilename(filename)) {
                throw new IllegalArgumentException("Filename already exists: " + filename);
            }

            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            video.setFilename(filename);
            video.setSizeBytes(Files.size(target));
        } else {
            throw new IllegalArgumentException("file is required");
        }

        // handle optional cover image upload
        if (request.getCoverImageFile() != null && !request.getCoverImageFile().isEmpty()) {
            MultipartFile cover = request.getCoverImageFile();
            String coverOriginal = cover.getOriginalFilename();
            String coverFilename;
            if (coverOriginal == null || coverOriginal.isBlank()) {
                coverFilename = System.currentTimeMillis() + "-cover";
            } else {
                coverFilename = coverOriginal.replaceAll("\\s+", "-");
            }
            Path imagesDir = Path.of(uploadProperties.getDirectory(), "images");
            Files.createDirectories(imagesDir);
            Path coverTarget = imagesDir.resolve(coverFilename);

            // if filename exists, auto-rename by appending a numeric suffix
            if (Files.exists(coverTarget) || videoRepository.existsByFilename(coverFilename)) {
                throw new IllegalArgumentException("Video Cover image already exists: " + coverFilename);
            }

            Files.copy(cover.getInputStream(), coverTarget, StandardCopyOption.REPLACE_EXISTING);
            video.setCoverImageFilename(coverFilename);
        }

        Timestamp now = new Timestamp(System.currentTimeMillis());
        video.setCreatedAt(now);
        video.setUpdatedAt(now);
        video.setCreatedBy(userId);
        video.setUpdatedBy(userId);

        Video saved = videoRepository.save(video);
        return toResponse(saved);
    }

    public VideoResponse updateVideo(String id, VideoUpdateRequest request, String userId) {
        Optional<Video> opt = videoRepository.findById(id);
        if (opt.isEmpty()) return null;
        Video video = opt.get();
        if (request.getName() != null) video.setName(request.getName());
    // originalUrl and sourceName removed
        if (request.getFilename() != null) video.setFilename(request.getFilename());
    if (request.getCoverImageFilename() != null) video.setCoverImageFilename(request.getCoverImageFilename());
    // width/height/duration fields removed
        if (request.getDescription() != null) video.setDescription(request.getDescription());
        if (request.getTags() != null) video.setTags(request.getTags());
        if (request.getLang() != null) video.setLang(request.getLang());
        if (request.getDisplayOrder() != null) video.setDisplayOrder(request.getDisplayOrder());
        if (request.getIsActive() != null) video.setIsActive(request.getIsActive());

        video.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        video.setUpdatedBy(userId);
        Video saved = videoRepository.save(video);
        return toResponse(saved);
    }

    public VideoResponse getVideoById(String id) {
        Optional<Video> opt = videoRepository.findById(id);
        return opt.map(this::toResponse).orElse(null);
    }

    public List<VideoResponse> listVideos() {
    List<Video> all = videoRepository.findAll();
    return all.stream().map(this::toResponse).toList();
    }

    public java.io.File getVideoFileByFilename(String filename) throws java.io.IOException {
        if (filename == null) throw new IllegalArgumentException("filename is null");
        Path videoPath = Path.of(uploadProperties.getDirectory(), filename);
        if (!Files.exists(videoPath)) {
            throw new IllegalArgumentException("Video file not found: " + filename);
        }
        return videoPath.toFile();
    }

    public org.springframework.core.io.Resource getVideoResource(String filename) throws java.io.IOException {
        if (filename == null) throw new IllegalArgumentException("filename is null");
        Path videoPath = Path.of(uploadProperties.getDirectory(), filename);
        if (!Files.exists(videoPath)) {
            throw new IllegalArgumentException("Video file not found: " + filename);
        }
        java.net.URI uri = videoPath.toUri();
        return new org.springframework.core.io.UrlResource(uri);
    }

    public java.io.File getCoverImageFileByFilename(String filename) throws java.io.IOException {
        if (filename == null) throw new IllegalArgumentException("filename is null");
        Path coverPath = Path.of(uploadProperties.getDirectory(), "images", filename);
        if (!Files.exists(coverPath)) {
            throw new IllegalArgumentException("Cover image file not found: " + filename);
        }
        return coverPath.toFile();
    }

    public boolean deleteVideo(String id, String userId) {
        Optional<Video> opt = videoRepository.findById(id);
        if (opt.isEmpty()) return false;
        Video video = opt.get();
        video.setIsActive(false);
        video.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        video.setUpdatedBy(userId);
        videoRepository.save(video);
        return true;
    }

    public List<VideoResponse> searchVideos(String name, Video.Language lang, String tags, Boolean isActive) {
    List<Video> list = videoRepository.searchVideos(name, lang, tags, isActive);
    return list.stream().map(this::toResponse).toList();
    }

    private VideoResponse toResponse(Video v) {
        VideoResponse r = new VideoResponse();
        r.setId(v.getId());
        r.setName(v.getName());
        r.setFilename(v.getFilename());
        r.setSizeBytes(v.getSizeBytes());
    r.setCoverImageFilename(v.getCoverImageFilename());
    // originalUrl, sourceName, width, height, duration removed
        r.setDescription(v.getDescription());
        r.setTags(v.getTags());
        r.setLang(v.getLang());
        r.setDisplayOrder(v.getDisplayOrder());
        r.setCreatedBy(v.getCreatedBy());
        r.setUpdatedBy(v.getUpdatedBy());
        r.setIsActive(v.getIsActive());
        if (v.getCreatedAt() != null) r.setCreatedAt(v.getCreatedAt().toString());
        if (v.getUpdatedAt() != null) r.setUpdatedAt(v.getUpdatedAt().toString());
        return r;
    }
}
