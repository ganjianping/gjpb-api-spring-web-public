package org.ganjp.blog.open.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ganjp.blog.cms.model.entity.*;
import org.ganjp.blog.common.model.ApiResponse;
import org.ganjp.blog.open.model.PaginatedResponse;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.List;
import org.ganjp.blog.open.service.PublicCmsService;
import org.ganjp.blog.cms.model.dto.ArticleResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;

import java.util.Locale;

@RestController
@RequestMapping("/v1/public/cms")
@RequiredArgsConstructor
@Slf4j
public class PublicCmsController {
    private final PublicCmsService publicCmsService;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;
    @Value("${logo.base-url:}")
    private String logoBaseUrl;
    @Value("${image.base-url:}")
    private String imageBaseUrl;
    @Value("${video.base-url:}")
    private String videoBaseUrl;
    @Value("${audio.base-url:}")
    private String audioBaseUrl;
    @Value("${article.cover-image-base-url:}")
    private String articleCoverImageBaseUrl;
    @Value("${file.base-url:}")
    private String fileBaseUrl;

    // websites
    @GetMapping("/websites")
    public ApiResponse<PaginatedResponse<?>> getWebsites(@RequestParam(required = false) String name,
                                                         @RequestParam(required = false) String lang,
                                                         @RequestParam(required = false) String tags,
                                                         @RequestParam(required = false) Boolean isActive,
                                                         @RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "20") int size) {
        Website.Language l = null;
        if (lang != null && !lang.isBlank()) {
            try { l = Website.Language.valueOf(lang.toUpperCase(Locale.ROOT)); } catch (IllegalArgumentException ex) { return ApiResponse.error(400, "Invalid lang", null); }
        }
        var respRaw = publicCmsService.getWebsites(name, l, tags, isActive, page, size);
        var resp = sanitizeWebsites(respRaw);
        return ApiResponse.success(resp, "Websites retrieved");
    }

    @GetMapping("/images")
    public ApiResponse<PaginatedResponse<?>> getImages(@RequestParam(required = false) String name,
                                                       @RequestParam(required = false) String lang,
                                                       @RequestParam(required = false) String tags,
                                                       @RequestParam(required = false) Boolean isActive,
                                                       @RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "20") int size) {
        org.ganjp.blog.cms.model.entity.Image.Language l = null;
        if (lang != null && !lang.isBlank()) {
            try { l = org.ganjp.blog.cms.model.entity.Image.Language.valueOf(lang.toUpperCase(Locale.ROOT)); } catch (IllegalArgumentException ex) { return ApiResponse.error(400, "Invalid lang", null); }
        }
        var respRaw = publicCmsService.getImages(name, l, tags, isActive, page, size);
        var resp = sanitizeImages(respRaw);
        return ApiResponse.success(resp, "Images retrieved");
    }
    /**
     * Sanitize images: create url and thumbnailUrl by prefixing imageBaseUrl to
     * filename and thumbnailFilename when they are not absolute, then remove
     * the original filename fields and other internal props.
     */
    private <T> PaginatedResponse<Map<String, Object>> sanitizeImages(PaginatedResponse<T> raw) {
        List<Map<String, Object>> list = raw.getContent().stream()
            .map(item -> objectMapper.convertValue(item, new TypeReference<Map<String, Object>>() {}))
            .filter(m -> {
                Object isActive = m.get("isActive");
                return isActive == null || Boolean.TRUE.equals(isActive);
            })
            .map(m -> {
                // Build full url from filename
                Object fnameObj = m.get("filename");
                if (fnameObj instanceof String) {
                    String fname = (String) fnameObj;
                    if (!fname.isBlank() && imageBaseUrl != null && !imageBaseUrl.isBlank()) {
                        String prefix = imageBaseUrl;
                        if (!prefix.endsWith("/") && !fname.startsWith("/")) prefix = prefix + "/";
                        else if (prefix.endsWith("/") && fname.startsWith("/")) fname = fname.substring(1);
                        m.put("url", prefix + fname);
                    } else if (!fname.isBlank() && (fname.startsWith("http") || fname.startsWith("/"))) {
                        m.put("url", fname);
                    }
                }

                // Build thumbnailUrl from thumbnailFilename
                Object tnameObj = m.get("thumbnailFilename");
                if (tnameObj instanceof String) {
                    String tname = (String) tnameObj;
                    if (!tname.isBlank() && imageBaseUrl != null && !imageBaseUrl.isBlank()) {
                        String prefix = imageBaseUrl;
                        if (!prefix.endsWith("/") && !tname.startsWith("/")) prefix = prefix + "/";
                        else if (prefix.endsWith("/") && tname.startsWith("/")) tname = tname.substring(1);
                        m.put("thumbnailUrl", prefix + tname);
                    } else if (!tname.isBlank() && (tname.startsWith("http") || tname.startsWith("/"))) {
                        m.put("thumbnailUrl", tname);
                    }
                }

                // remove original filename keys and other internal props
                m.remove("filename");
                m.remove("thumbnailFilename");
                m.remove("isActive");
                m.remove("createdAt");
                m.remove("createdBy");
                m.remove("updatedBy");
                m.remove("tagsArray");
                m.remove("content");
                return m;
            })
            .collect(Collectors.toList());

        return PaginatedResponse.of(list, raw.getPage(), raw.getSize(), list.size());
    }

    @GetMapping("/logos")
    public ApiResponse<PaginatedResponse<?>> getLogos(@RequestParam(required = false) String name,
                                                      @RequestParam(required = false) String lang,
                                                      @RequestParam(required = false) String tags,
                                                      @RequestParam(required = false) Boolean isActive,
                                                      @RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "20") int size) {
        org.ganjp.blog.cms.model.entity.Image.Language l = null;
        if (lang != null && !lang.isBlank()) {
            try { l = org.ganjp.blog.cms.model.entity.Image.Language.valueOf(lang.toUpperCase(Locale.ROOT)); } catch (IllegalArgumentException ex) { return ApiResponse.error(400, "Invalid lang", null); }
        }
        var respRaw = publicCmsService.getLogos(name, l, tags, isActive, page, size);
        var resp = sanitizeLogos(respRaw);
        return ApiResponse.success(resp, "Logos retrieved");
    }

    @GetMapping("/videos")
    public ApiResponse<PaginatedResponse<?>> getVideos(@RequestParam(required = false) String name,
                                                       @RequestParam(required = false) String lang,
                                                       @RequestParam(required = false) String tags,
                                                       @RequestParam(required = false) Boolean isActive,
                                                       @RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "20") int size) {
        org.ganjp.blog.cms.model.entity.Video.Language l = null;
        if (lang != null && !lang.isBlank()) {
            try { l = org.ganjp.blog.cms.model.entity.Video.Language.valueOf(lang.toUpperCase(Locale.ROOT)); } catch (IllegalArgumentException ex) { return ApiResponse.error(400, "Invalid lang", null); }
        }
        var respRaw = publicCmsService.getVideos(name, l, tags, isActive, page, size);
        var resp = sanitizeVideos(respRaw);
        return ApiResponse.success(resp, "Videos retrieved");
    }

    @GetMapping("/files")
    public ApiResponse<PaginatedResponse<?>> getFiles(@RequestParam(required = false) String name,
                                                      @RequestParam(required = false) String lang,
                                                      @RequestParam(required = false) String tags,
                                                      @RequestParam(required = false) Boolean isActive,
                                                      @RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "20") int size) {
        org.ganjp.blog.cms.model.entity.File.Language l = null;
        if (lang != null && !lang.isBlank()) {
            try { l = org.ganjp.blog.cms.model.entity.File.Language.valueOf(lang.toUpperCase(Locale.ROOT)); } catch (IllegalArgumentException ex) { return ApiResponse.error(400, "Invalid lang", null); }
        }
        var respRaw = publicCmsService.getFiles(name, l, tags, isActive, page, size);
        var resp = sanitizeFiles(respRaw);
        return ApiResponse.success(resp, "Files retrieved");
    }

    /**
     * Sanitize files: create url by prefixing fileBaseUrl to filename when it is not absolute,
     * then remove the original filename field and other internal props.
     */
    private <T> PaginatedResponse<Map<String, Object>> sanitizeFiles(PaginatedResponse<T> raw) {
        List<Map<String, Object>> list = raw.getContent().stream()
            .map(item -> objectMapper.convertValue(item, new TypeReference<Map<String, Object>>() {}))
            .filter(m -> {
                Object isActive = m.get("isActive");
                return isActive == null || Boolean.TRUE.equals(isActive);
            })
            .map(m -> {
                Object fnameObj = m.get("filename");
                if (fnameObj instanceof String) {
                    String fname = (String) fnameObj;
                    if (!fname.isBlank() && fileBaseUrl != null && !fileBaseUrl.isBlank()) {
                        String prefix = fileBaseUrl;
                        if (!prefix.endsWith("/") && !fname.startsWith("/")) prefix = prefix + "/";
                        else if (prefix.endsWith("/") && fname.startsWith("/")) fname = fname.substring(1);
                        m.put("url", prefix + fname);
                    } else if (!fname.isBlank() && (fname.startsWith("http") || fname.startsWith("/"))) {
                        m.put("url", fname);
                    }
                }

                m.remove("filename");
                m.remove("isActive");
                m.remove("createdAt");
                m.remove("createdBy");
                m.remove("updatedBy");
                m.remove("tagsArray");
                m.remove("content");
                return m;
            })
            .collect(Collectors.toList());

        return PaginatedResponse.of(list, raw.getPage(), raw.getSize(), list.size());
    }

    @GetMapping("/audios")
    public ApiResponse<PaginatedResponse<?>> getAudios(@RequestParam(required = false) String name,
                                                       @RequestParam(required = false) String lang,
                                                       @RequestParam(required = false) String tags,
                                                       @RequestParam(required = false) Boolean isActive,
                                                       @RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "20") int size) {
        org.ganjp.blog.cms.model.entity.Audio.Language l = null;
        if (lang != null && !lang.isBlank()) {
            try { l = org.ganjp.blog.cms.model.entity.Audio.Language.valueOf(lang.toUpperCase(Locale.ROOT)); } catch (IllegalArgumentException ex) { return ApiResponse.error(400, "Invalid lang", null); }
        }
        var respRaw = publicCmsService.getAudios(name, l, tags, isActive, page, size);
        var resp = sanitizeAudios(respRaw);
        return ApiResponse.success(resp, "Audios retrieved");
    }

    @GetMapping("/articles")
    public ApiResponse<PaginatedResponse<?>> getArticles(@RequestParam(required = false) String title,
                                                         @RequestParam(required = false) String lang,
                                                         @RequestParam(required = false) String tags,
                                                         @RequestParam(required = false) Boolean isActive,
                                                         @RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "20") int size) {
        org.ganjp.blog.cms.model.entity.Article.Language l = null;
        if (lang != null && !lang.isBlank()) {
            try { l = org.ganjp.blog.cms.model.entity.Article.Language.valueOf(lang.toUpperCase(Locale.ROOT)); } catch (IllegalArgumentException ex) { return ApiResponse.error(400, "Invalid lang", null); }
        }
        var respRaw = publicCmsService.getArticles(title, l, tags, isActive, page, size);
        var resp = sanitizeArticles(respRaw);
        return ApiResponse.success(resp, "Articles retrieved");
    }

    @GetMapping("/articles/{id}")
    public ApiResponse<ArticleResponse> getArticleById(@PathVariable String id) {
        ArticleResponse r = publicCmsService.getArticleById(id);
        if (r == null) return ApiResponse.error(404, "Article not found", null);

        // Build coverImageUrl from coverImageFilename using configured base if needed
        String cimg = r.getCoverImageFilename();
        if (cimg != null && !cimg.isBlank()) {
            if (articleCoverImageBaseUrl != null && !articleCoverImageBaseUrl.isBlank()) {
                String prefix = articleCoverImageBaseUrl;
                if (!prefix.endsWith("/") && !cimg.startsWith("/")) prefix = prefix + "/";
                else if (prefix.endsWith("/") && cimg.startsWith("/")) cimg = cimg.substring(1);
                r.setCoverImageUrl(prefix + cimg);
            } else if (cimg.startsWith("http") || cimg.startsWith("/")) {
                r.setCoverImageUrl(cimg);
            }
        }

        // Remove filename from response
        r.setCoverImageFilename(null);

        return ApiResponse.success(r, "Article retrieved");
    }

    private <T> PaginatedResponse<Map<String, Object>> sanitizePaginated(PaginatedResponse<T> raw) {
    List<Map<String, Object>> list = raw.getContent().stream()
        .map(item -> objectMapper.convertValue(item, new TypeReference<Map<String, Object>>() {}))
                .filter(m -> {
                    Object isActive = m.get("isActive");
                    return isActive == null || Boolean.TRUE.equals(isActive);
                })
                .map(m -> {
                    m.remove("isActive");
                    m.remove("createdAt");
                    m.remove("createdBy");
                    m.remove("updatedBy");
                    m.remove("tagsArray");
                    m.remove("content");
                    return m;
                })
                .collect(Collectors.toList());

        return PaginatedResponse.of(list, raw.getPage(), raw.getSize(), list.size());
    }

    /**
     * Similar to sanitizePaginated but also ensures website logoUrl is absolute by
     * prepending configured logoBaseUrl when the value does not start with http.
     */
    private <T> PaginatedResponse<Map<String, Object>> sanitizeWebsites(PaginatedResponse<T> raw) {
        List<Map<String, Object>> list = raw.getContent().stream()
            .map(item -> objectMapper.convertValue(item, new TypeReference<Map<String, Object>>() {}))
            .filter(m -> {
                Object isActive = m.get("isActive");
                return isActive == null || Boolean.TRUE.equals(isActive);
            })
            .map(m -> {
                // Keep logoUrl absolute by prefixing logoBaseUrl when needed
                Object logoObj = m.get("logoUrl");
                if (logoObj instanceof String) {
                    String logo = (String) logoObj;
                    if (!logo.isBlank() && !logo.startsWith("http") && logoBaseUrl != null && !logoBaseUrl.isBlank()) {
                        String prefix = logoBaseUrl;
                        // Ensure no double slash when concatenating
                        if (!prefix.endsWith("/") && !logo.startsWith("/")) prefix = prefix + "/";
                        else if (prefix.endsWith("/") && logo.startsWith("/")) logo = logo.substring(1);
                        m.put("logoUrl", prefix + logo);
                    }
                }

                m.remove("isActive");
                m.remove("createdAt");
                m.remove("createdBy");
                m.remove("updatedBy");
                m.remove("tagsArray");
                m.remove("content");
                return m;
            })
            .collect(Collectors.toList());

        return PaginatedResponse.of(list, raw.getPage(), raw.getSize(), list.size());
    }

    /**
     * Sanitize logos: create url and thumbnailUrl by prefixing logoBaseUrl to
     * filename and thumbnailFilename when they are not absolute, then remove
     * the original filename fields and other internal props.
     */
    private <T> PaginatedResponse<Map<String, Object>> sanitizeLogos(PaginatedResponse<T> raw) {
        List<Map<String, Object>> list = raw.getContent().stream()
            .map(item -> objectMapper.convertValue(item, new TypeReference<Map<String, Object>>() {}))
            .filter(m -> {
                Object isActive = m.get("isActive");
                return isActive == null || Boolean.TRUE.equals(isActive);
            })
            .map(m -> {
                // Build full url from filename
                Object fnameObj = m.get("filename");
                if (fnameObj instanceof String) {
                    String fname = (String) fnameObj;
                    if (!fname.isBlank() && logoBaseUrl != null && !logoBaseUrl.isBlank()) {
                        String prefix = logoBaseUrl;
                        if (!prefix.endsWith("/") && !fname.startsWith("/")) prefix = prefix + "/";
                        else if (prefix.endsWith("/") && fname.startsWith("/")) fname = fname.substring(1);
                        m.put("url", prefix + fname);
                    } else if (!fname.isBlank() && (fname.startsWith("http") || fname.startsWith("/"))) {
                        // if filename already contains absolute or starts with slash and no base is set, keep as-is in url
                        m.put("url", fname);
                    }
                }

                // Build thumbnailUrl from thumbnailFilename
                Object tnameObj = m.get("thumbnailFilename");
                if (tnameObj instanceof String) {
                    String tname = (String) tnameObj;
                    if (!tname.isBlank() && logoBaseUrl != null && !logoBaseUrl.isBlank()) {
                        String prefix = logoBaseUrl;
                        if (!prefix.endsWith("/") && !tname.startsWith("/")) prefix = prefix + "/";
                        else if (prefix.endsWith("/") && tname.startsWith("/")) tname = tname.substring(1);
                        m.put("thumbnailUrl", prefix + tname);
                    } else if (!tname.isBlank() && (tname.startsWith("http") || tname.startsWith("/"))) {
                        m.put("thumbnailUrl", tname);
                    }
                }

                // remove original filename keys and other internal props
                m.remove("filename");
                m.remove("thumbnailFilename");
                m.remove("isActive");
                m.remove("createdAt");
                m.remove("createdBy");
                m.remove("updatedBy");
                m.remove("tagsArray");
                m.remove("content");
                return m;
            })
            .collect(Collectors.toList());

        return PaginatedResponse.of(list, raw.getPage(), raw.getSize(), list.size());
    }

    /**
     * Sanitize videos: create url and coverImageUrl by prefixing videoBaseUrl to
     * filename and coverImageFilename (cover-images path) when they are not absolute,
     * then remove the original filename fields and other internal props.
     */
    private <T> PaginatedResponse<Map<String, Object>> sanitizeVideos(PaginatedResponse<T> raw) {
        List<Map<String, Object>> list = raw.getContent().stream()
            .map(item -> objectMapper.convertValue(item, new TypeReference<Map<String, Object>>() {}))
            .filter(m -> {
                Object isActive = m.get("isActive");
                return isActive == null || Boolean.TRUE.equals(isActive);
            })
            .map(m -> {
                // Build full video url from filename
                Object fnameObj = m.get("filename");
                if (fnameObj instanceof String) {
                    String fname = (String) fnameObj;
                    if (!fname.isBlank() && videoBaseUrl != null && !videoBaseUrl.isBlank()) {
                        String prefix = videoBaseUrl;
                        if (!prefix.endsWith("/") && !fname.startsWith("/")) prefix = prefix + "/";
                        else if (prefix.endsWith("/") && fname.startsWith("/")) fname = fname.substring(1);
                        m.put("url", prefix + fname);
                    } else if (!fname.isBlank() && (fname.startsWith("http") || fname.startsWith("/"))) {
                        m.put("url", fname);
                    }
                }

                // Build coverImageUrl from coverImageFilename under cover-images path
                Object cimgObj = m.get("coverImageFilename");
                if (cimgObj instanceof String) {
                    String cimg = (String) cimgObj;
                    if (!cimg.isBlank() && videoBaseUrl != null && !videoBaseUrl.isBlank()) {
                        String prefix = videoBaseUrl;
                        if (!prefix.endsWith("/")) prefix = prefix + "/";
                        // ensure 'cover-images/' segment
                        if (!prefix.endsWith("cover-images/")) {
                            if (!prefix.endsWith("/")) prefix = prefix + "cover-images/";
                            else prefix = prefix + "cover-images/";
                        }
                        if (cimg.startsWith("/")) cimg = cimg.substring(1);
                        m.put("coverImageUrl", prefix + cimg);
                    } else if (!cimg.isBlank() && (cimg.startsWith("http") || cimg.startsWith("/"))) {
                        m.put("coverImageUrl", cimg);
                    }
                }

                // remove original filename keys and other internal props
                m.remove("filename");
                m.remove("coverImageFilename");
                m.remove("isActive");
                m.remove("createdAt");
                m.remove("createdBy");
                m.remove("updatedBy");
                m.remove("tagsArray");
                m.remove("content");
                return m;
            })
            .collect(Collectors.toList());

        return PaginatedResponse.of(list, raw.getPage(), raw.getSize(), list.size());
    }

    /**
     * Sanitize audios: create url and coverImageUrl by prefixing audioBaseUrl to
     * filename and coverImageFilename (cover-images path) when they are not absolute,
     * then remove the original filename fields and other internal props.
     */
    private <T> PaginatedResponse<Map<String, Object>> sanitizeAudios(PaginatedResponse<T> raw) {
        List<Map<String, Object>> list = raw.getContent().stream()
            .map(item -> objectMapper.convertValue(item, new TypeReference<Map<String, Object>>() {}))
            .filter(m -> {
                Object isActive = m.get("isActive");
                return isActive == null || Boolean.TRUE.equals(isActive);
            })
            .map(m -> {
                // Build full audio url from filename
                Object fnameObj = m.get("filename");
                if (fnameObj instanceof String) {
                    String fname = (String) fnameObj;
                    if (!fname.isBlank() && audioBaseUrl != null && !audioBaseUrl.isBlank()) {
                        String prefix = audioBaseUrl;
                        if (!prefix.endsWith("/") && !fname.startsWith("/")) prefix = prefix + "/";
                        else if (prefix.endsWith("/") && fname.startsWith("/")) fname = fname.substring(1);
                        m.put("url", prefix + fname);
                    } else if (!fname.isBlank() && (fname.startsWith("http") || fname.startsWith("/"))) {
                        m.put("url", fname);
                    }
                }

                // Build coverImageUrl from coverImageFilename under cover-images path
                Object cimgObj = m.get("coverImageFilename");
                if (cimgObj instanceof String) {
                    String cimg = (String) cimgObj;
                    if (!cimg.isBlank() && audioBaseUrl != null && !audioBaseUrl.isBlank()) {
                        String prefix = audioBaseUrl;
                        if (!prefix.endsWith("/")) prefix = prefix + "/";
                        if (!prefix.endsWith("cover-images/")) {
                            if (!prefix.endsWith("/")) prefix = prefix + "cover-images/";
                            else prefix = prefix + "cover-images/";
                        }
                        if (cimg.startsWith("/")) cimg = cimg.substring(1);
                        m.put("coverImageUrl", prefix + cimg);
                    } else if (!cimg.isBlank() && (cimg.startsWith("http") || cimg.startsWith("/"))) {
                        m.put("coverImageUrl", cimg);
                    }
                }

                // remove original filename keys and other internal props
                m.remove("filename");
                m.remove("coverImageFilename");
                m.remove("isActive");
                m.remove("createdAt");
                m.remove("createdBy");
                m.remove("updatedBy");
                m.remove("tagsArray");
                m.remove("content");
                return m;
            })
            .collect(Collectors.toList());

        return PaginatedResponse.of(list, raw.getPage(), raw.getSize(), list.size());
    }

    /**
     * Sanitize articles: create coverImageUrl by prefixing articleCoverImageBaseUrl to
     * coverImageFilename when it is not absolute, then remove the original
     * coverImageFilename field and other internal props.
     */
    private <T> PaginatedResponse<Map<String, Object>> sanitizeArticles(PaginatedResponse<T> raw) {
        List<Map<String, Object>> list = raw.getContent().stream()
            .map(item -> objectMapper.convertValue(item, new TypeReference<Map<String, Object>>() {}))
            .filter(m -> {
                Object isActive = m.get("isActive");
                return isActive == null || Boolean.TRUE.equals(isActive);
            })
            .map(m -> {
                Object cimgObj = m.get("coverImageFilename");
                if (cimgObj instanceof String) {
                    String cimg = (String) cimgObj;
                    if (!cimg.isBlank() && articleCoverImageBaseUrl != null && !articleCoverImageBaseUrl.isBlank()) {
                        String prefix = articleCoverImageBaseUrl;
                        if (!prefix.endsWith("/") && !cimg.startsWith("/")) prefix = prefix + "/";
                        else if (prefix.endsWith("/") && cimg.startsWith("/")) cimg = cimg.substring(1);
                        m.put("coverImageUrl", prefix + cimg);
                    } else if (!cimg.isBlank() && (cimg.startsWith("http") || cimg.startsWith("/"))) {
                        m.put("coverImageUrl", cimg);
                    }
                }

                m.remove("coverImageFilename");
                m.remove("isActive");
                m.remove("createdAt");
                m.remove("createdBy");
                m.remove("updatedBy");
                m.remove("tagsArray");
                m.remove("content");
                return m;
            })
            .collect(Collectors.toList());

        return PaginatedResponse.of(list, raw.getPage(), raw.getSize(), list.size());
    }
}
