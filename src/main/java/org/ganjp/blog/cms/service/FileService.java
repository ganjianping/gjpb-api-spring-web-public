package org.ganjp.blog.cms.service;

import lombok.RequiredArgsConstructor;
import org.ganjp.blog.cms.config.FileUploadProperties;
import org.ganjp.blog.cms.model.dto.FileCreateRequest;
import org.ganjp.blog.cms.model.dto.FileResponse;
import org.ganjp.blog.cms.model.dto.FileUpdateRequest;
import org.ganjp.blog.cms.model.entity.File;
import org.ganjp.blog.cms.repository.FileRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {
    private final FileRepository fileRepository;
    private final FileUploadProperties uploadProperties; // file upload config

    public FileResponse createFile(FileCreateRequest request, String userId) {
        File f = new File();
        String id = UUID.randomUUID().toString();
        f.setId(id);
        f.setName(request.getName());
        f.setOriginalUrl(request.getOriginalUrl());
        f.setSourceName(request.getSourceName());
        f.setTags(request.getTags());
        if (request.getLang() != null) f.setLang(request.getLang());
        if (request.getDisplayOrder() != null) f.setDisplayOrder(request.getDisplayOrder());

        try {
            String baseDir = uploadProperties.getDirectory();
            Path filesDir = Path.of(baseDir);
            Files.createDirectories(filesDir);

            if (request.getFile() != null && !request.getFile().isEmpty()) {
                MultipartFile mf = request.getFile();
                String orig = mf.getOriginalFilename();
                String stored = (request.getFilename() != null && !request.getFilename().isBlank()) ? request.getFilename() : (orig == null ? System.currentTimeMillis()+"-file" : orig.replaceAll("\\s+", "-"));
                Path target = filesDir.resolve(stored);
                int suffix = 1;
                String base = stored;
                String ext = "";
                int dot = stored.lastIndexOf('.');
                if (dot > 0) { base = stored.substring(0, dot); ext = stored.substring(dot); }
                while (Files.exists(target)) {
                    stored = base + "-" + suffix + ext;
                    target = filesDir.resolve(stored);
                    suffix++;
                }
                Files.copy(mf.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
                f.setFilename(stored);
                f.setSizeBytes(mf.getSize());
                if (dot > 0 && dot < stored.length()-1) f.setExtension(stored.substring(dot+1));
                f.setMimeType(mf.getContentType());
            } else if (request.getOriginalUrl() != null && !request.getOriginalUrl().isBlank()) {
                String url = request.getOriginalUrl();
                String stored = request.getFilename();
                if (stored == null || stored.isBlank()) {
                    try { java.net.URL u = new java.net.URL(url); String p = u.getPath(); int last = p.lastIndexOf('/'); String lastSeg = last>=0? p.substring(last+1): p; if (lastSeg==null||lastSeg.isBlank()) lastSeg = System.currentTimeMillis()+"-file"; stored = lastSeg.replaceAll("\\s+","-"); } catch (Exception ex) { stored = System.currentTimeMillis()+"-file"; }
                }
                Path target = filesDir.resolve(stored);
                int suffix = 1;
                String base = stored;
                String ext = "";
                int dot = stored.lastIndexOf('.');
                if (dot>0) { base = stored.substring(0,dot); ext = stored.substring(dot); }
                while (Files.exists(target)) {
                    stored = base + "-" + suffix + ext;
                    target = filesDir.resolve(stored);
                    suffix++;
                }
                try (java.io.InputStream is = new java.net.URL(url).openStream()) {
                    byte[] data = is.readAllBytes();
                    Files.write(target, data);
                    f.setSizeBytes((long) data.length);
                    if (dot>0 && dot < stored.length()-1) f.setExtension(stored.substring(dot+1));
                }
                f.setFilename(stored);
                f.setOriginalUrl(request.getOriginalUrl());
            } else if (request.getFilename() != null) {
                f.setFilename(request.getFilename());
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to save file: " + e.getMessage());
        }

        Timestamp now = new Timestamp(System.currentTimeMillis());
        f.setCreatedAt(now);
        f.setUpdatedAt(now);
        f.setCreatedBy(userId);
        f.setUpdatedBy(userId);

        File saved = fileRepository.save(f);
        return toResponse(saved);
    }

    public FileResponse updateFile(String id, FileUpdateRequest request, String userId) {
        Optional<File> opt = fileRepository.findById(id);
        if (opt.isEmpty()) return null;
        File f = opt.get();
        if (request.getName() != null) f.setName(request.getName());
        if (request.getOriginalUrl() != null) f.setOriginalUrl(request.getOriginalUrl());
        if (request.getSourceName() != null) f.setSourceName(request.getSourceName());
        if (request.getFilename() != null) f.setFilename(request.getFilename());

        try {
            String baseDir = uploadProperties.getDirectory();
            Path filesDir = Path.of(baseDir);
            Files.createDirectories(filesDir);

            if (request.getFile() != null && !request.getFile().isEmpty()) {
                MultipartFile mf = request.getFile();
                String orig = mf.getOriginalFilename();
                String stored = (request.getFilename() != null && !request.getFilename().isBlank()) ? request.getFilename() : (orig == null ? System.currentTimeMillis()+"-file" : orig.replaceAll("\\s+", "-"));
                Path target = filesDir.resolve(stored);
                int suffix = 1;
                String base = stored;
                String ext = "";
                int dot = stored.lastIndexOf('.');
                if (dot > 0) { base = stored.substring(0, dot); ext = stored.substring(dot); }
                while (Files.exists(target)) {
                    stored = base + "-" + suffix + ext;
                    target = filesDir.resolve(stored);
                    suffix++;
                }
                if (f.getFilename() != null) { try { Path old = filesDir.resolve(f.getFilename()); Files.deleteIfExists(old); } catch (IOException ignored) {} }
                Files.copy(mf.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
                f.setFilename(stored);
                f.setSizeBytes(mf.getSize());
                if (dot > 0 && dot < stored.length()-1) f.setExtension(stored.substring(dot+1));
                f.setMimeType(mf.getContentType());
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to save file: " + e.getMessage());
        }

        if (request.getTags() != null) f.setTags(request.getTags());
        if (request.getLang() != null) f.setLang(request.getLang());
        if (request.getDisplayOrder() != null) f.setDisplayOrder(request.getDisplayOrder());
        if (request.getIsActive() != null) f.setIsActive(request.getIsActive());

        f.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        f.setUpdatedBy(userId);
        File saved = fileRepository.save(f);
        return toResponse(saved);
    }

    public FileResponse getFileById(String id) {
        Optional<File> opt = fileRepository.findById(id);
        return opt.map(this::toResponse).orElse(null);
    }

    public java.io.File getFileByFilename(String filename) {
        if (filename == null) throw new IllegalArgumentException("filename is null");
        Path p = Path.of(uploadProperties.getDirectory(), filename);
        if (!Files.exists(p)) throw new IllegalArgumentException("File not found: " + filename);
        return p.toFile();
    }

    public java.util.List<FileResponse> listFiles() {
        List<File> all = fileRepository.findAll();
        return all.stream().map(this::toResponse).toList();
    }

    public boolean deleteFile(String id, String userId) {
        Optional<File> opt = fileRepository.findById(id);
        if (opt.isEmpty()) return false;
        File f = opt.get();
        f.setIsActive(false);
        f.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        f.setUpdatedBy(userId);
        fileRepository.save(f);
        return true;
    }

    private FileResponse toResponse(File f) {
        FileResponse r = new FileResponse();
        r.setId(f.getId());
        r.setName(f.getName());
        r.setOriginalUrl(f.getOriginalUrl());
        r.setSourceName(f.getSourceName());
        r.setFilename(f.getFilename());
        r.setSizeBytes(f.getSizeBytes());
        r.setExtension(f.getExtension());
        r.setMimeType(f.getMimeType());
        r.setTags(f.getTags());
        r.setLang(f.getLang());
        r.setDisplayOrder(f.getDisplayOrder());
        r.setCreatedBy(f.getCreatedBy());
        r.setUpdatedBy(f.getUpdatedBy());
        r.setIsActive(f.getIsActive());
        if (f.getCreatedAt() != null) r.setCreatedAt(f.getCreatedAt().toString());
        if (f.getUpdatedAt() != null) r.setUpdatedAt(f.getUpdatedAt().toString());
        return r;
    }
}
