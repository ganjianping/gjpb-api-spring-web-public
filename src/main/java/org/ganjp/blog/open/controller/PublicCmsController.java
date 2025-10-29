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

import java.util.Locale;

@RestController
@RequestMapping("/v1/public/cms")
@RequiredArgsConstructor
@Slf4j
public class PublicCmsController {
    private final PublicCmsService publicCmsService;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

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
        var resp = sanitizePaginated(respRaw);
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
        var resp = sanitizePaginated(respRaw);
        return ApiResponse.success(resp, "Images retrieved");
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
        var resp = sanitizePaginated(respRaw);
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
        var resp = sanitizePaginated(respRaw);
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
        var resp = sanitizePaginated(respRaw);
        return ApiResponse.success(resp, "Files retrieved");
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
        var resp = sanitizePaginated(respRaw);
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
        var resp = sanitizePaginated(respRaw);
        return ApiResponse.success(resp, "Articles retrieved");
    }

    @GetMapping("/articles/{id}")
    public ApiResponse<ArticleResponse> getArticleById(@PathVariable String id) {
        ArticleResponse r = publicCmsService.getArticleById(id);
        if (r == null) return ApiResponse.error(404, "Article not found", null);
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
}
