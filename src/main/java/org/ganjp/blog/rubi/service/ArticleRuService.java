package org.ganjp.blog.rubi.service;

import lombok.RequiredArgsConstructor;
import org.ganjp.blog.rubi.config.ArticleRuProperties;
import org.ganjp.blog.rubi.model.dto.ArticleRuCreateRequest;
import org.ganjp.blog.rubi.model.dto.ArticleRuResponse;
import org.ganjp.blog.rubi.model.dto.ArticleRuUpdateRequest;
import org.ganjp.blog.rubi.model.entity.ArticleRu;
import org.ganjp.blog.rubi.repository.ArticleRuRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
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
public class ArticleRuService {
    private final ArticleRuRepository articleRepository;
    private final ArticleRuProperties articleProperties;

    public ArticleRuResponse createArticle(ArticleRuCreateRequest request, String userId) {
        ArticleRu a = new ArticleRu();
        String id = UUID.randomUUID().toString();
        a.setId(id);
        a.setTitle(request.getTitle());
        a.setSummary(request.getSummary());
        a.setContent(request.getContent());
        a.setOriginalUrl(request.getOriginalUrl());
        a.setSourceName(request.getSourceName());
        a.setTerm(request.getTerm());
        a.setWeek(request.getWeek());
        a.setTags(request.getTags());
        if (request.getLang() != null) a.setLang(request.getLang());
        if (request.getDisplayOrder() != null) a.setDisplayOrder(request.getDisplayOrder());
        if (request.getIsActive() != null) a.setIsActive(request.getIsActive());
        if (request.getCoverImageOriginalUrl() != null) a.setCoverImageOriginalUrl(request.getCoverImageOriginalUrl());
        // cover image
        try {
            // determine article upload directory (from article.cover-image.upload.directory)
            String articleCoverImageDir = articleProperties.getCoverImage().getUpload().getDirectory();

            if (request.getCoverImageFile() != null && !request.getCoverImageFile().isEmpty()) {
                MultipartFile cover = request.getCoverImageFile();
                String coverOriginal = cover.getOriginalFilename();
                String coverFilename;
                if (coverOriginal == null || coverOriginal.isBlank()) {
                    coverFilename = System.currentTimeMillis() + "-cover";
                } else {
                    coverFilename = coverOriginal.replaceAll("\\s+", "-");
                }
                Path imagesDir = Path.of(articleCoverImageDir);
                Files.createDirectories(imagesDir);
                Path coverTarget = imagesDir.resolve(coverFilename);

                if (Files.exists(coverTarget)) {
                    throw new IllegalArgumentException("Cover image already exists: " + coverFilename);
                }

                try {
                    BufferedImage original = ImageIO.read(cover.getInputStream());
                    if (original != null) {
                        BufferedImage resized = resizeImageIfNeeded(original, articleProperties.getCoverImage().getUpload().getResize().getMaxSize());
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
                a.setCoverImageFilename(coverFilename);
            } else if (request.getCoverImageOriginalUrl() != null && !request.getCoverImageOriginalUrl().isBlank()) {
                // download remote image and save it
                String url = request.getCoverImageOriginalUrl();
                String coverFilename = request.getCoverImageFilename();
                if (coverFilename == null || coverFilename.isBlank()) {
                    // derive filename from URL
                    try {
                        java.net.URL u = new java.net.URL(url);
                        String p = u.getPath();
                        int last = p.lastIndexOf('/');
                        String lastSeg = last >= 0 ? p.substring(last + 1) : p;
                        if (lastSeg == null || lastSeg.isBlank()) lastSeg = System.currentTimeMillis() + "-cover";
                        coverFilename = lastSeg.replaceAll("\\s+", "-");
                    } catch (Exception ex) {
                        coverFilename = System.currentTimeMillis() + "-cover";
                    }
                }

                Path imagesDir = Path.of(articleCoverImageDir);
                Files.createDirectories(imagesDir);
                Path coverTarget = imagesDir.resolve(coverFilename);

                // ensure unique filename
                int suffix = 1;
                String base = coverFilename;
                String ext = "";
                int dot = coverFilename.lastIndexOf('.');
                if (dot > 0) {
                    base = coverFilename.substring(0, dot);
                    ext = coverFilename.substring(dot);
                }
                while (Files.exists(coverTarget)) {
                    coverFilename = base + "-" + suffix + ext;
                    coverTarget = imagesDir.resolve(coverFilename);
                    suffix++;
                }

                try (java.io.InputStream is = new java.net.URL(url).openStream()) {
                    // try to read as image
                    try {
                        byte[] data = is.readAllBytes();
                        java.io.ByteArrayInputStream bis = new java.io.ByteArrayInputStream(data);
                        BufferedImage original = ImageIO.read(bis);
                        if (original != null) {
                            BufferedImage resized = resizeImageIfNeeded(original, articleProperties.getCoverImage().getUpload().getResize().getMaxSize());
                            String writeExt = "png";
                            if (dot > 0 && dot < coverFilename.length() - 1) writeExt = coverFilename.substring(dot + 1).toLowerCase();
                            ImageIO.write(resized, writeExt, coverTarget.toFile());
                        } else {
                            // fallback - write raw bytes
                            Files.write(coverTarget, data);
                        }
                    } catch (IOException ex) {
                        // fallback - stream copy
                        try (java.io.InputStream is2 = new java.net.URL(url).openStream()) {
                            Files.copy(is2, coverTarget, StandardCopyOption.REPLACE_EXISTING);
                        }
                    }
                }

                a.setCoverImageFilename(coverFilename);
                a.setCoverImageOriginalUrl(request.getCoverImageOriginalUrl());
            } else if (request.getCoverImageFilename() != null) {
                a.setCoverImageFilename(request.getCoverImageFilename());
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to save cover image: " + e.getMessage());
        }

        Timestamp now = new Timestamp(System.currentTimeMillis());
        a.setCreatedAt(now);
        a.setUpdatedAt(now);
        a.setCreatedBy(userId);
        a.setUpdatedBy(userId);

        ArticleRu saved = articleRepository.save(a);
        return toResponse(saved);
    }

    public ArticleRuResponse updateArticle(String id, ArticleRuUpdateRequest request, String userId) {
        Optional<ArticleRu> opt = articleRepository.findById(id);
        if (opt.isEmpty()) return null;
        ArticleRu a = opt.get();

        if ("null".equals(request.getOriginalUrl())) {
            a.setOriginalUrl(null);
            request.setOriginalUrl(null);
        }
        if ("null".equals(request.getSourceName())) {
            a.setSourceName(null);
            request.setSourceName(null);
        }
        if ("null".equals(request.getCoverImageOriginalUrl())) {
            a.setCoverImageOriginalUrl(null);
            request.setCoverImageOriginalUrl(null);
        }
        if ("null".equals(request.getCoverImageFilename())) {
            a.setCoverImageFilename(null);
            request.setCoverImageFilename(null);
        }

        if (request.getTitle() != null) a.setTitle(request.getTitle());
        if (request.getSummary() != null) a.setSummary(request.getSummary());
        if (request.getContent() != null) a.setContent(request.getContent());
        if (request.getOriginalUrl() != null) a.setOriginalUrl(request.getOriginalUrl());
        if (request.getSourceName() != null) a.setSourceName(request.getSourceName());
        // capture existing cover imgage original URL to decide whether we need to re-download
        String existingCoverOriginalUrl = a.getCoverImageOriginalUrl();
        // do NOT set coverImageOriginalUrl here unconditionally — handle it only when the URL changes (to avoid re-downloading)

        try {
            String articleCoverImageDir = articleProperties.getCoverImage().getUpload().getDirectory();
            if (request.getCoverImageFile() != null && !request.getCoverImageFile().isEmpty()) {
                MultipartFile coverFile = request.getCoverImageFile();
                String coverOriginalFilename = coverFile.getOriginalFilename();
                String coverFilename;
                if (coverOriginalFilename == null || coverOriginalFilename.isBlank()) {
                    coverFilename = System.currentTimeMillis() + "-cover";
                } else {
                    coverFilename = coverOriginalFilename.replaceAll("\\s+", "-");
                }
                Path imagesDir = Path.of(articleCoverImageDir);
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
                while (Files.exists(coverTarget)) {
                    coverFilename = base + "-" + suffix + ext;
                    coverTarget = imagesDir.resolve(coverFilename);
                    suffix++;
                }

                if (a.getCoverImageFilename() != null) {
                    try { Path old = imagesDir.resolve(a.getCoverImageFilename()); Files.deleteIfExists(old); } catch (IOException ignored) {}
                }

                try {
                    BufferedImage original = ImageIO.read(coverFile.getInputStream());
                    if (original != null) {
                        BufferedImage resized = resizeImageIfNeeded(original, articleProperties.getCoverImage().getUpload().getResize().getMaxSize());
                        String writeExt = "png";
                        if (dot > 0 && dot < coverFilename.length() - 1) writeExt = coverFilename.substring(dot + 1).toLowerCase();
                        ImageIO.write(resized, writeExt, coverTarget.toFile());
                    } else {
                        Files.copy(coverFile.getInputStream(), coverTarget, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException e) {
                    Files.copy(coverFile.getInputStream(), coverTarget, StandardCopyOption.REPLACE_EXISTING);
                }

                a.setCoverImageFilename(coverFilename);
            } else if (request.getCoverImageOriginalUrl() != null && !request.getCoverImageOriginalUrl().isBlank()) {
                // download remote image and replace only if the original URL changed
                String url = request.getCoverImageOriginalUrl();
                // if the URL is identical to the existing one, skip downloading
                if (url.equals(existingCoverOriginalUrl)) {
                    // nothing to do — keep existing cover image file and filename
                } else {
                    String coverFilename = request.getCoverImageFilename();
                    if (coverFilename == null || coverFilename.isBlank()) {
                        try {
                            java.net.URL u = new java.net.URL(url);
                            String p = u.getPath();
                            int last = p.lastIndexOf('/');
                            String lastSeg = last >= 0 ? p.substring(last + 1) : p;
                            if (lastSeg == null || lastSeg.isBlank()) lastSeg = System.currentTimeMillis() + "-cover";
                            coverFilename = lastSeg.replaceAll("\\s+", "-");
                        } catch (Exception ex) {
                            coverFilename = System.currentTimeMillis() + "-cover";
                        }
                    }

                    Path imagesDir = Path.of(articleCoverImageDir);
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
                    while (Files.exists(coverTarget)) {
                        coverFilename = base + "-" + suffix + ext;
                        coverTarget = imagesDir.resolve(coverFilename);
                        suffix++;
                    }

                    if (a.getCoverImageFilename() != null) {
                        try { Path old = imagesDir.resolve(a.getCoverImageFilename()); Files.deleteIfExists(old); } catch (IOException ignored) {}
                    }

                    try (java.io.InputStream is = new java.net.URL(url).openStream()) {
                        try {
                            byte[] data = is.readAllBytes();
                            java.io.ByteArrayInputStream bis = new java.io.ByteArrayInputStream(data);
                            BufferedImage original = ImageIO.read(bis);
                            if (original != null) {
                                BufferedImage resized = resizeImageIfNeeded(original, articleProperties.getCoverImage().getUpload().getResize().getMaxSize());
                                String writeExt = "png";
                                if (dot > 0 && dot < coverFilename.length() - 1) writeExt = coverFilename.substring(dot + 1).toLowerCase();
                                ImageIO.write(resized, writeExt, coverTarget.toFile());
                            } else {
                                Files.write(coverTarget, data);
                            }
                        } catch (IOException ex) {
                            try (java.io.InputStream is2 = new java.net.URL(url).openStream()) {
                                Files.copy(is2, coverTarget, StandardCopyOption.REPLACE_EXISTING);
                            }
                        }
                    }

                    a.setCoverImageFilename(coverFilename);
                    a.setCoverImageOriginalUrl(request.getCoverImageOriginalUrl());
                }
            }

            // handle cover image filename change only (rename existing file)
            if (request.getCoverImageFilename() != null &&
                    request.getCoverImageFilename().lastIndexOf('.') > 0 &&
                    !request.getCoverImageFilename().equals(a.getCoverImageFilename())) {
                // change the image file name in local storage only (no re-download), implying a rename
                Path imagesDir = Path.of(articleProperties.getCoverImage().getUpload().getDirectory());
                Path oldPath = imagesDir.resolve(a.getCoverImageFilename());
                Path newPath = imagesDir.resolve(request.getCoverImageFilename());
                // if newPath exists, it will not be overwritten
                if (Files.exists(newPath)) {
                    throw new IllegalArgumentException("Cover image file with name " + request.getCoverImageFilename() + " already exists");
                }
                
                if (Files.exists(oldPath)) {
                    Files.move(oldPath, newPath, StandardCopyOption.REPLACE_EXISTING);
                }

                a.setCoverImageFilename(request.getCoverImageFilename());
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to save cover image: " + e.getMessage());
        }

        if (request.getTerm() != null) a.setTerm(request.getTerm());
        if (request.getWeek() != null) a.setWeek(request.getWeek());
        if (request.getTags() != null) a.setTags(request.getTags());
        if (request.getLang() != null) a.setLang(request.getLang());
        if (request.getDisplayOrder() != null) a.setDisplayOrder(request.getDisplayOrder());
        if (request.getIsActive() != null) a.setIsActive(request.getIsActive());

        a.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        a.setUpdatedBy(userId);
        ArticleRu saved = articleRepository.save(a);
        return toResponse(saved);
    }

    public ArticleRuResponse getArticleById(String id) {
        Optional<ArticleRu> opt = articleRepository.findById(id);
        return opt.map(this::toResponse).orElse(null);
    }

    public List<ArticleRuResponse> listArticles() {
        List<ArticleRu> all = articleRepository.findAll();
        return all.stream().map(this::toResponse).toList();
    }

    public boolean deleteArticle(String id, String userId) {
        Optional<ArticleRu> opt = articleRepository.findById(id);
        if (opt.isEmpty()) return false;
        ArticleRu a = opt.get();
        a.setIsActive(false);
        a.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        a.setUpdatedBy(userId);
        articleRepository.save(a);
        return true;
    }

    public java.io.File getCoverImageFileByFilename(String filename) {
        if (filename == null) throw new IllegalArgumentException("filename is null");
        Path coverPath = Path.of(articleProperties.getCoverImage().getUpload().getDirectory(), "", filename);
        if (!Files.exists(coverPath)) {
            throw new IllegalArgumentException("Cover image file not found: " + filename);
        }
        return coverPath.toFile();
    }

    public Page<ArticleRuResponse> searchArticles(String title, org.ganjp.blog.rubi.model.entity.ArticleRu.Language lang, String tags, Boolean isActive, Pageable pageable) {
        Page<ArticleRu> page = articleRepository.searchArticles(title, lang, tags, isActive, pageable);
        return page.map(this::toResponse);
    }

    private ArticleRuResponse toResponse(ArticleRu a) {
        ArticleRuResponse r = new ArticleRuResponse();
        r.setId(a.getId());
        r.setTitle(a.getTitle());
        r.setSummary(a.getSummary());
        r.setContent(a.getContent());
        r.setOriginalUrl(a.getOriginalUrl());
        r.setSourceName(a.getSourceName());
        r.setCoverImageFilename(a.getCoverImageFilename());
        if (a.getCoverImageFilename() != null) {
            r.setCoverImageFileUrl(articleProperties.getCoverImage().getBaseUrl() + "/" + a.getCoverImageFilename());
        }
    r.setCoverImageOriginalUrl(a.getCoverImageOriginalUrl());
        r.setTerm(a.getTerm());
        r.setWeek(a.getWeek());
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
