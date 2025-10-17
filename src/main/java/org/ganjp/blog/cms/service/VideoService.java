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
        video.setOriginalUrl(request.getOriginalUrl());
        video.setSourceName(request.getSourceName());
        video.setCoverImageUrl(request.getCoverImageUrl());
        video.setWidth(request.getWidth());
        video.setHeight(request.getHeight());
        video.setDuration(request.getDuration());
        video.setDescription(request.getDescription());
        video.setTags(request.getTags());
        if (request.getLang() != null) video.setLang(request.getLang());
        if (request.getDisplayOrder() != null) video.setDisplayOrder(request.getDisplayOrder());
        if (request.getIsActive() != null) video.setIsActive(request.getIsActive());

        // handle file upload if present
        if (request.getFile() != null && !request.getFile().isEmpty()) {
            MultipartFile file = request.getFile();
            String originalFilename = file.getOriginalFilename();
            String filename = System.currentTimeMillis() + "-" + (originalFilename == null ? "video" : originalFilename.replaceAll("\\s+", "-"));
            Path videoDir = Path.of(uploadProperties.getDirectory());
            Files.createDirectories(videoDir);
            Path target = videoDir.resolve(filename);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            video.setFilename(filename);
            video.setSizeBytes(Files.size(target));
        } else if (request.getOriginalUrl() != null && !request.getOriginalUrl().isBlank()) {
            // store originalUrl as reference; filename may be null
            video.setFilename(null);
            video.setSizeBytes(null);
        } else {
            throw new IllegalArgumentException("Either file or originalUrl must be provided");
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
        if (request.getOriginalUrl() != null) video.setOriginalUrl(request.getOriginalUrl());
        if (request.getSourceName() != null) video.setSourceName(request.getSourceName());
        if (request.getFilename() != null) video.setFilename(request.getFilename());
        if (request.getCoverImageUrl() != null) video.setCoverImageUrl(request.getCoverImageUrl());
        if (request.getWidth() != null) video.setWidth(request.getWidth());
        if (request.getHeight() != null) video.setHeight(request.getHeight());
        if (request.getDuration() != null) video.setDuration(request.getDuration());
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
        Path videoPath = Path.of(uploadProperties.getDirectory(), "videos", filename);
        if (!Files.exists(videoPath)) {
            throw new IllegalArgumentException("Video file not found: " + filename);
        }
        return videoPath.toFile();
    }

    public org.springframework.core.io.Resource getVideoResource(String filename) throws java.io.IOException {
        if (filename == null) throw new IllegalArgumentException("filename is null");
        Path videoPath = Path.of(uploadProperties.getDirectory(), "videos", filename);
        if (!Files.exists(videoPath)) {
            throw new IllegalArgumentException("Video file not found: " + filename);
        }
        java.net.URI uri = videoPath.toUri();
        return new org.springframework.core.io.UrlResource(uri);
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
        r.setOriginalUrl(v.getOriginalUrl());
        r.setSourceName(v.getSourceName());
        r.setFilename(v.getFilename());
        r.setSizeBytes(v.getSizeBytes());
        r.setCoverImageUrl(v.getCoverImageUrl());
        r.setWidth(v.getWidth());
        r.setHeight(v.getHeight());
        r.setDuration(v.getDuration());
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
