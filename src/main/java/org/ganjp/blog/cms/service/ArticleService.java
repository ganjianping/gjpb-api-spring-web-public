package org.ganjp.blog.cms.service;

import lombok.RequiredArgsConstructor;
import org.ganjp.blog.cms.config.ArticleUploadProperties;
import org.ganjp.blog.cms.model.dto.ArticleCreateRequest;
import org.ganjp.blog.cms.model.dto.ArticleResponse;
import org.ganjp.blog.cms.model.dto.ArticleUpdateRequest;
import org.ganjp.blog.cms.model.entity.Article;
import org.ganjp.blog.cms.repository.ArticleRepository;
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
public class ArticleService {
    private final ArticleRepository articleRepository;
    private final ArticleUploadProperties uploadProperties; // article-specific upload config

    public ArticleResponse createArticle(ArticleCreateRequest request, String userId) {
        Article a = new Article();
        String id = UUID.randomUUID().toString();
        a.setId(id);
        a.setTitle(request.getTitle());
        a.setSummary(request.getSummary());
        a.setContent(request.getContent());
        a.setOriginalUrl(request.getOriginalUrl());
        a.setSourceName(request.getSourceName());
        a.setTags(request.getTags());
        if (request.getLang() != null) a.setLang(request.getLang());
        if (request.getDisplayOrder() != null) a.setDisplayOrder(request.getDisplayOrder());
        if (request.getIsActive() != null) a.setIsActive(request.getIsActive());
        if (request.getCoverImageOriginalUrl() != null) a.setCoverImageOriginalUrl(request.getCoverImageOriginalUrl());
        // cover image
        try {
            // determine article upload directory (from article.upload.directory)
            String articleDir = uploadProperties.getDirectory();

            if (request.getCoverImageFile() != null && !request.getCoverImageFile().isEmpty()) {
                MultipartFile cover = request.getCoverImageFile();
                String coverOriginal = cover.getOriginalFilename();
                String coverFilename;
                if (coverOriginal == null || coverOriginal.isBlank()) {
                    coverFilename = System.currentTimeMillis() + "-cover";
                } else {
                    coverFilename = coverOriginal.replaceAll("\\s+", "-");
                }
                Path imagesDir = Path.of(articleDir, "cover-images");
                Files.createDirectories(imagesDir);
                Path coverTarget = imagesDir.resolve(coverFilename);

                if (Files.exists(coverTarget)) {
                    throw new IllegalArgumentException("Cover image already exists: " + coverFilename);
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

                Path imagesDir = Path.of(articleDir, "cover-images");
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
                            BufferedImage resized = resizeImageIfNeeded(original, uploadProperties.getCoverImage().getMaxSize());
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

        Article saved = articleRepository.save(a);
        return toResponse(saved);
    }

    public ArticleResponse updateArticle(String id, ArticleUpdateRequest request, String userId) {
        Optional<Article> opt = articleRepository.findById(id);
        if (opt.isEmpty()) return null;
        Article a = opt.get();
        if (request.getTitle() != null) a.setTitle(request.getTitle());
        if (request.getSummary() != null) a.setSummary(request.getSummary());
        if (request.getContent() != null) a.setContent(request.getContent());
        if (request.getOriginalUrl() != null) a.setOriginalUrl(request.getOriginalUrl());
        if (request.getSourceName() != null) a.setSourceName(request.getSourceName());
    if (request.getCoverImageFilename() != null) a.setCoverImageFilename(request.getCoverImageFilename());
    if (request.getCoverImageOriginalUrl() != null) a.setCoverImageOriginalUrl(request.getCoverImageOriginalUrl());

        try {
            String articleDir = uploadProperties.getDirectory();
            if (request.getCoverImageFile() != null && !request.getCoverImageFile().isEmpty()) {
                MultipartFile cover = request.getCoverImageFile();
                String coverOriginal = cover.getOriginalFilename();
                String coverFilename;
                if (coverOriginal == null || coverOriginal.isBlank()) {
                    coverFilename = System.currentTimeMillis() + "-cover";
                } else {
                    coverFilename = coverOriginal.replaceAll("\\s+", "-");
                }
                Path imagesDir = Path.of(articleDir, "cover-images");
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

                a.setCoverImageFilename(coverFilename);
            } else if (request.getCoverImageOriginalUrl() != null && !request.getCoverImageOriginalUrl().isBlank()) {
                // download remote image and replace
                String url = request.getCoverImageOriginalUrl();
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

                Path imagesDir = Path.of(articleDir, "cover-images");
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
                            BufferedImage resized = resizeImageIfNeeded(original, uploadProperties.getCoverImage().getMaxSize());
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
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to save cover image: " + e.getMessage());
        }

        if (request.getTags() != null) a.setTags(request.getTags());
        if (request.getLang() != null) a.setLang(request.getLang());
        if (request.getDisplayOrder() != null) a.setDisplayOrder(request.getDisplayOrder());
        if (request.getIsActive() != null) a.setIsActive(request.getIsActive());

        a.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        a.setUpdatedBy(userId);
        Article saved = articleRepository.save(a);
        return toResponse(saved);
    }

    public ArticleResponse getArticleById(String id) {
        Optional<Article> opt = articleRepository.findById(id);
        return opt.map(this::toResponse).orElse(null);
    }

    public List<ArticleResponse> listArticles() {
        List<Article> all = articleRepository.findAll();
        return all.stream().map(this::toResponse).toList();
    }

    public boolean deleteArticle(String id, String userId) {
        Optional<Article> opt = articleRepository.findById(id);
        if (opt.isEmpty()) return false;
        Article a = opt.get();
        a.setIsActive(false);
        a.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        a.setUpdatedBy(userId);
        articleRepository.save(a);
        return true;
    }

    public java.io.File getCoverImageFileByFilename(String filename) {
        if (filename == null) throw new IllegalArgumentException("filename is null");
        Path coverPath = Path.of(uploadProperties.getDirectory(), "cover-images", filename);
        if (!Files.exists(coverPath)) {
            throw new IllegalArgumentException("Cover image file not found: " + filename);
        }
        return coverPath.toFile();
    }

    public List<ArticleResponse> searchArticles(String title, org.ganjp.blog.cms.model.entity.Article.Language lang, String tags, Boolean isActive) {
        List<Article> list = articleRepository.searchArticles(title, lang, tags, isActive);
        return list.stream().map(this::toResponse).toList();
    }

    private ArticleResponse toResponse(Article a) {
        ArticleResponse r = new ArticleResponse();
        r.setId(a.getId());
        r.setTitle(a.getTitle());
        r.setSummary(a.getSummary());
        r.setContent(a.getContent());
        r.setOriginalUrl(a.getOriginalUrl());
        r.setSourceName(a.getSourceName());
        r.setCoverImageFilename(a.getCoverImageFilename());
    r.setCoverImageOriginalUrl(a.getCoverImageOriginalUrl());
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
