package org.ganjp.blog.cms.service;

import lombok.RequiredArgsConstructor;
import org.ganjp.blog.cms.config.AudioUploadProperties;
import org.ganjp.blog.cms.model.dto.AudioCreateRequest;
import org.ganjp.blog.cms.model.dto.AudioResponse;
import org.ganjp.blog.cms.model.dto.AudioUpdateRequest;
import org.ganjp.blog.cms.model.entity.Audio;
import org.ganjp.blog.cms.repository.AudioRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AudioService {
    private final AudioRepository audioRepository;
    private final AudioUploadProperties uploadProperties;

    public AudioResponse createAudio(AudioCreateRequest request, String userId) throws IOException {
        Audio audio = new Audio();
        String id = UUID.randomUUID().toString();
        audio.setId(id);
        audio.setName(request.getName());
        audio.setCoverImageFilename(request.getCoverImageFilename());
        if (request.getOriginalUrl() != null) audio.setOriginalUrl(request.getOriginalUrl());
        if (request.getSourceName() != null) audio.setSourceName(request.getSourceName());
    audio.setDescription(request.getDescription());
    if (request.getSubtitle() != null) audio.setSubtitle(request.getSubtitle());
        audio.setTags(request.getTags());
        if (request.getLang() != null) audio.setLang(request.getLang());
        if (request.getDisplayOrder() != null) audio.setDisplayOrder(request.getDisplayOrder());
        if (request.getIsActive() != null) audio.setIsActive(request.getIsActive());

        if (request.getFile() != null && !request.getFile().isEmpty()) {
            MultipartFile file = request.getFile();
            String originalFilename = file.getOriginalFilename();
            String filename;
            if (originalFilename == null || originalFilename.isBlank()) {
                filename = System.currentTimeMillis() + "-audio";
            } else {
                filename = originalFilename.replaceAll("\\s+", "-");
            }
            Path audioDir = Path.of(uploadProperties.getDirectory());
            Files.createDirectories(audioDir);
            Path target = audioDir.resolve(filename);

            if (Files.exists(target) || audioRepository.existsByFilename(filename)) {
                throw new IllegalArgumentException("Filename already exists: " + filename);
            }

            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            audio.setFilename(filename);
            audio.setSizeBytes(Files.size(target));
        } else {
            throw new IllegalArgumentException("file is required");
        }

        // cover image
        if (request.getCoverImageFile() != null && !request.getCoverImageFile().isEmpty()) {
            MultipartFile cover = request.getCoverImageFile();
            String coverOriginal = cover.getOriginalFilename();
            String coverFilename;
            if (coverOriginal == null || coverOriginal.isBlank()) {
                coverFilename = System.currentTimeMillis() + "-cover";
            } else {
                coverFilename = coverOriginal.replaceAll("\\s+", "-");
            }
            Path imagesDir = Path.of(uploadProperties.getDirectory(), "cover-images");
            Files.createDirectories(imagesDir);
            Path coverTarget = imagesDir.resolve(coverFilename);

            if (Files.exists(coverTarget) || audioRepository.existsByFilename(coverFilename)) {
                throw new IllegalArgumentException("Audio Cover image already exists: " + coverFilename);
            }

            try {
                BufferedImage original = ImageIO.read(cover.getInputStream());
                if (original != null) {
                    BufferedImage resized = resizeImageIfNeeded(original, uploadProperties.getCoverImage().getMaxSize());
                    String ext = "png";
                    int dot = coverFilename.lastIndexOf('.');
                    if (dot > 0 && dot < coverFilename.length() - 1) ext = coverFilename.substring(dot + 1).toLowerCase();
                    ImageIO.write(resized, ext, coverTarget.toFile());
                } else {
                    Files.copy(cover.getInputStream(), coverTarget, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                Files.copy(cover.getInputStream(), coverTarget, StandardCopyOption.REPLACE_EXISTING);
            }
            audio.setCoverImageFilename(coverFilename);
        }

        Timestamp now = new Timestamp(System.currentTimeMillis());
        audio.setCreatedAt(now);
        audio.setUpdatedAt(now);
        audio.setCreatedBy(userId);
        audio.setUpdatedBy(userId);

        Audio saved = audioRepository.save(audio);
        return toResponse(saved);
    }

    public AudioResponse updateAudio(String id, AudioUpdateRequest request, String userId) {
        Optional<Audio> opt = audioRepository.findById(id);
        if (opt.isEmpty()) return null;
        Audio audio = opt.get();
        if (request.getName() != null) audio.setName(request.getName());
        if (request.getFilename() != null) audio.setFilename(request.getFilename());
        if (request.getOriginalUrl() != null) audio.setOriginalUrl(request.getOriginalUrl());
        if (request.getSourceName() != null) audio.setSourceName(request.getSourceName());
        if (request.getCoverImageFilename() != null) audio.setCoverImageFilename(request.getCoverImageFilename());

        try {
            if (request.getCoverImageFile() != null && !request.getCoverImageFile().isEmpty()) {
                MultipartFile cover = request.getCoverImageFile();
                String coverOriginal = cover.getOriginalFilename();
                String coverFilename;
                if (coverOriginal == null || coverOriginal.isBlank()) {
                    coverFilename = System.currentTimeMillis() + "-cover";
                } else {
                    coverFilename = coverOriginal.replaceAll("\\s+", "-");
                }
                Path imagesDir = Path.of(uploadProperties.getDirectory(), "cover-images");
                Files.createDirectories(imagesDir);
                Path coverTarget = imagesDir.resolve(coverFilename);

                int suffix = 1;
                String base = coverFilename;
                String ext = "";
                int dot = coverFilename.lastIndexOf('.');
                if (dot > 0) {
                    base = coverFilename.substring(0, dot);
                    ext = coverFilename.substring(dot);
                }
                while (Files.exists(coverTarget) || audioRepository.existsByFilename(coverFilename)) {
                    coverFilename = base + "-" + suffix + ext;
                    coverTarget = imagesDir.resolve(coverFilename);
                    suffix++;
                }

                if (audio.getCoverImageFilename() != null) {
                    try { Path old = imagesDir.resolve(audio.getCoverImageFilename()); Files.deleteIfExists(old); } catch (IOException ignored) {}
                }

                try {
                    BufferedImage original = ImageIO.read(cover.getInputStream());
                    if (original != null) {
                        BufferedImage resized = resizeImageIfNeeded(original, uploadProperties.getCoverImage().getMaxSize());
                        String writeExt = "png";
                        if (dot > 0 && dot < coverFilename.length() - 1) writeExt = coverFilename.substring(dot + 1).toLowerCase();
                        ImageIO.write(resized, writeExt, coverTarget.toFile());
                    } else {
                        Files.copy(cover.getInputStream(), coverTarget, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException e) {
                    Files.copy(cover.getInputStream(), coverTarget, StandardCopyOption.REPLACE_EXISTING);
                }
                audio.setCoverImageFilename(coverFilename);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to save cover image: " + e.getMessage());
        }

    if (request.getDescription() != null) audio.setDescription(request.getDescription());
    if (request.getSubtitle() != null) audio.setSubtitle(request.getSubtitle());
        if (request.getTags() != null) audio.setTags(request.getTags());
        if (request.getLang() != null) audio.setLang(request.getLang());
        if (request.getDisplayOrder() != null) audio.setDisplayOrder(request.getDisplayOrder());
        if (request.getIsActive() != null) audio.setIsActive(request.getIsActive());

        audio.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        audio.setUpdatedBy(userId);
        Audio saved = audioRepository.save(audio);
        return toResponse(saved);
    }

    public AudioResponse getAudioById(String id) {
        Optional<Audio> opt = audioRepository.findById(id);
        return opt.map(this::toResponse).orElse(null);
    }

    public List<AudioResponse> listAudios() {
        List<Audio> all = audioRepository.findAll();
        return all.stream().map(this::toResponse).toList();
    }

    public java.io.File getAudioFileByFilename(String filename) throws java.io.IOException {
        if (filename == null) throw new IllegalArgumentException("filename is null");
        Path audioPath = Path.of(uploadProperties.getDirectory(), filename);
        if (!Files.exists(audioPath)) {
            throw new IllegalArgumentException("Audio file not found: " + filename);
        }
        return audioPath.toFile();
    }

    public java.io.File getCoverImageFileByFilename(String filename) throws java.io.IOException {
        if (filename == null) throw new IllegalArgumentException("filename is null");
        Path coverPath = Path.of(uploadProperties.getDirectory(), "cover-images", filename);
        if (!Files.exists(coverPath)) {
            throw new IllegalArgumentException("Cover image file not found: " + filename);
        }
        return coverPath.toFile();
    }

    public boolean deleteAudio(String id, String userId) {
        Optional<Audio> opt = audioRepository.findById(id);
        if (opt.isEmpty()) return false;
        Audio audio = opt.get();
        audio.setIsActive(false);
        audio.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        audio.setUpdatedBy(userId);
        audioRepository.save(audio);
        return true;
    }

    public List<AudioResponse> searchAudios(String name, org.ganjp.blog.cms.model.entity.Video.Language lang, String tags, Boolean isActive) {
        List<Audio> list = audioRepository.searchAudios(name, lang, tags, isActive);
        return list.stream().map(this::toResponse).toList();
    }

    private AudioResponse toResponse(Audio a) {
        AudioResponse r = new AudioResponse();
        r.setId(a.getId());
        r.setName(a.getName());
        r.setFilename(a.getFilename());
        r.setSizeBytes(a.getSizeBytes());
        r.setCoverImageFilename(a.getCoverImageFilename());
    r.setOriginalUrl(a.getOriginalUrl());
    r.setSourceName(a.getSourceName());
    r.setSubtitle(a.getSubtitle());
    r.setDescription(a.getDescription());
        r.setTags(a.getTags());
        r.setLang(a.getLang());
        r.setDisplayOrder(a.getDisplayOrder());
        r.setCreatedBy(a.getCreatedBy());
        r.setUpdatedBy(a.getUpdatedBy());
        r.setIsActive(a.getIsActive());
        if (a.getCreatedAt() != null) r.setCreatedAt(a.getCreatedAt().toString());
        if (a.getUpdatedAt() != null) r.setUpdatedAt(a.getUpdatedAt().toString());
        return r;
    }

    private BufferedImage resizeImageIfNeeded(BufferedImage image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();
        if (width <= maxSize && height <= maxSize) return image;
        float scale = Math.min((float) maxSize / width, (float) maxSize / height);
        int newWidth = Math.round(width * scale);
        int newHeight = Math.round(height * scale);
        BufferedImage resized = new BufferedImage(newWidth, newHeight, image.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : image.getType());
        resized.getGraphics().drawImage(image, 0, 0, newWidth, newHeight, null);
        return resized;
    }
}
