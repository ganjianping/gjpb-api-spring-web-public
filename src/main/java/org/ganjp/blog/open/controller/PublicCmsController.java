package org.ganjp.blog.open.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ganjp.blog.cms.model.entity.*;
import org.ganjp.blog.common.model.ApiResponse;
import org.ganjp.blog.open.model.PaginatedResponse;
import org.ganjp.blog.open.service.PublicCmsService;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

@RestController
@RequestMapping("/v1/public/cms")
@RequiredArgsConstructor
@Slf4j
public class PublicCmsController {
    private final PublicCmsService publicCmsService;

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
        var resp = publicCmsService.getWebsites(name, l, tags, isActive, page, size);
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
        var resp = publicCmsService.getImages(name, l, tags, isActive, page, size);
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
        var resp = publicCmsService.getLogos(name, l, tags, isActive, page, size);
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
        var resp = publicCmsService.getVideos(name, l, tags, isActive, page, size);
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
        var resp = publicCmsService.getFiles(name, l, tags, isActive, page, size);
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
        var resp = publicCmsService.getAudios(name, l, tags, isActive, page, size);
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
        var resp = publicCmsService.getArticles(title, l, tags, isActive, page, size);
        return ApiResponse.success(resp, "Articles retrieved");
    }
}
