package org.ganjp.blog.open.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ganjp.blog.cms.config.ArticleProperties;
import org.ganjp.blog.cms.model.dto.*;
import org.ganjp.blog.cms.model.entity.Image;
import org.ganjp.blog.cms.model.entity.Website;
import org.ganjp.blog.cms.model.entity.Question;
import org.ganjp.blog.cms.service.*;
import org.ganjp.blog.cms.service.LogoService;
import org.ganjp.blog.cms.model.dto.LogoResponse;
import org.ganjp.blog.open.model.PublicLogoResponse;
import org.ganjp.blog.common.model.PaginatedResponse;
import org.ganjp.blog.open.model.PublicArticleDetailResponse;
import org.ganjp.blog.open.model.PublicArticleResponse;
import org.ganjp.blog.open.model.PublicVideoResponse;
import org.ganjp.blog.open.model.PublicAudioResponse;
import org.ganjp.blog.open.model.PublicFileResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PublicCmsService {
    private final WebsiteService websiteService;
    private final ImageService imageService;
    private final LogoProcessingService logoProcessingService;
    private final LogoService logoService;
    private final VideoService videoService;
    private final org.ganjp.blog.cms.service.FileService fileService;
    private final AudioService audioService;
    private final ArticleService articleService;
    private final QuestionService questionService;
    private final org.ganjp.blog.cms.config.ArticleProperties articleProperties;
    @Value("${image.base-url:}")
    private String imageBaseUrl;
    @Value("${logo.base-url:}")
    private String logoBaseUrl;
    @Value("${video.base-url:}")
    private String videoBaseUrl;
    @Value("${audio.base-url:}")
    private String audioBaseUrl;
    @Value("${file.base-url:}")
    private String fileBaseUrl;

    public PaginatedResponse<org.ganjp.blog.cms.model.dto.WebsiteResponse> getWebsites(String name, Website.Language lang, String tags, Boolean isActive, int page, int size) {
        var pageable = PageRequest.of(Math.max(0, page), Math.max(1, size));

        var pageResult = websiteService.getWebsites(name, lang, tags, isActive, pageable);
        return PaginatedResponse.of(pageResult.getContent(), pageResult.getNumber(), pageResult.getSize(), pageResult.getTotalElements());
    }

    private <T> PaginatedResponse<T> paginateList(List<T> all, int page, int size) {
        int p = Math.max(0, page);
        int s = Math.max(1, size);
        int from = p * s;
        long total = all.size();
        if (from >= all.size()) return PaginatedResponse.of(new ArrayList<>(), p, s, total);
        int to = Math.min(all.size(), from + s);
        List<T> sub = all.subList(from, to);
        return PaginatedResponse.of(sub, p, s, total);
    }

    /**
     * Join a base URL and a path (filename). Returns null if path is empty.
     * If base is blank and path is absolute (starts with http or /) returns path.
     */
    private String joinBaseAndPath(String base, String path) {
        if (path == null || path.isBlank()) return null;
        if (base != null && !base.isBlank()) {
            String prefix = base;
            String p = path;
            if (!prefix.endsWith("/") && !p.startsWith("/")) prefix = prefix + "/";
            else if (prefix.endsWith("/") && p.startsWith("/")) p = p.substring(1);
            return prefix + p;
        }
        if (path.startsWith("http") || path.startsWith("/")) return path;
        return null;
    }

    /**
     * Join base + segment + path. Segment should not be null (e.g. "cover-images").
     */
    private String joinBasePathWithSegment(String base, String segment, String path) {
        if (path == null || path.isBlank()) return null;
        if (segment == null) segment = "";
        // normalize
        String seg = segment;
        if (!seg.endsWith("/")) seg = seg + "/";
        if (base != null && !base.isBlank()) {
            String prefix = base;
            if (!prefix.endsWith("/")) prefix = prefix + "/";
            // avoid double slashes between prefix and seg
            if (prefix.endsWith("/") && seg.startsWith("/")) seg = seg.substring(1);
            String p = path.startsWith("/") ? path.substring(1) : path;
            return prefix + seg + p;
        }
        // no base; if path is absolute return as-is, else return seg+path with leading '/'
        if (path.startsWith("http") || path.startsWith("/")) return path;
        return "/" + seg + (path.startsWith("/") ? path.substring(1) : path);
    }

    public PaginatedResponse<org.ganjp.blog.open.model.PublicImageResponse> getImages(String name, Image.Language lang, String tags, Boolean isActive, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "displayOrder"));
        Page<ImageResponse> pageResult = imageService.searchImages(name, lang, tags, isActive, pageable);

        // Map internal ImageResponse -> PublicImageResponse and compute urls
        List<org.ganjp.blog.open.model.PublicImageResponse> publicList = pageResult.getContent().stream().map(r -> {
            org.ganjp.blog.open.model.PublicImageResponse.PublicImageResponseBuilder b = org.ganjp.blog.open.model.PublicImageResponse.builder()
                .id(r.getId())
                .name(r.getName())
                .originalUrl(r.getOriginalUrl())
                .altText(r.getAltText())
                .tags(r.getTags())
                .lang(r.getLang())
                .displayOrder(r.getDisplayOrder())
                .updatedAt(r.getUpdatedAt());

            String fname = r.getFilename();
            b.url(joinBaseAndPath(imageBaseUrl, fname));

            String tname = r.getThumbnailFilename();
            b.thumbnailUrl(joinBaseAndPath(imageBaseUrl, tname));

            return b.build();
        }).toList();

        return PaginatedResponse.of(publicList, page, size, pageResult.getTotalElements());
    }

    public PaginatedResponse<PublicLogoResponse> getLogos(String name, org.ganjp.blog.cms.model.entity.Logo.Language lang, String tags, Boolean isActive, int page, int size) {
        List<LogoResponse> all = logoService.searchLogos(name, lang, tags, isActive);

        List<PublicLogoResponse> publicList = all.stream().map(r -> {
            PublicLogoResponse.PublicLogoResponseBuilder b = PublicLogoResponse.builder()
                .id(r.getId())
                .name(r.getName())
                .tags(r.getTags())
                .lang(r.getLang())
                .displayOrder(r.getDisplayOrder())
                .updatedAt(r.getUpdatedAt() == null ? null : r.getUpdatedAt().toString());

            String fname = r.getFilename();
            String built = joinBaseAndPath(logoBaseUrl, fname);
            b.url(built);
            b.thumbnailUrl(built);

            return b.build();
        }).toList();

        return paginateList(publicList, page, size);
    }

    public PaginatedResponse<PublicVideoResponse> getVideos(String name, org.ganjp.blog.cms.model.entity.Video.Language lang, String tags, Boolean isActive, int page, int size) {
        List<org.ganjp.blog.cms.model.dto.VideoResponse> all = videoService.searchVideos(name, lang, tags, isActive);

        List<PublicVideoResponse> publicList = all.stream().map(r -> {
            PublicVideoResponse.PublicVideoResponseBuilder b = PublicVideoResponse.builder()
                .id(r.getId())
                .title(r.getName())
                .description(r.getDescription())
                .tags(r.getTags())
                .lang(r.getLang())
                .displayOrder(r.getDisplayOrder())
                .updatedAt(r.getUpdatedAt());

            String fname = r.getFilename();
            b.url(joinBaseAndPath(videoBaseUrl, fname));

            String cimg = r.getCoverImageFilename();
            b.coverImageUrl(joinBasePathWithSegment(videoBaseUrl, "cover-images", cimg));

            return b.build();
        }).toList();

        return paginateList(publicList, page, size);
    }

    public PaginatedResponse<PublicFileResponse> getFiles(String name, org.ganjp.blog.cms.model.entity.File.Language lang, String tags, Boolean isActive, int page, int size) {
        List<org.ganjp.blog.cms.model.dto.FileResponse> all = fileService.searchFiles(name, lang, tags, isActive);

        List<PublicFileResponse> publicList = all.stream().map(r -> {
            PublicFileResponse.PublicFileResponseBuilder b = PublicFileResponse.builder()
                .id(r.getId())
                .name(r.getName())
                .description(null)
                .originalUrl(r.getOriginalUrl())
                .tags(r.getTags())
                .lang(r.getLang())
                .displayOrder(r.getDisplayOrder())
                .updatedAt(r.getUpdatedAt());

            String fname = r.getFilename();
            b.url(joinBaseAndPath(fileBaseUrl, fname));

            return b.build();
        }).toList();

        return paginateList(publicList, page, size);
    }

    public PaginatedResponse<PublicAudioResponse> getAudios(String name, org.ganjp.blog.cms.model.entity.Audio.Language lang, String tags, Boolean isActive, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "displayOrder"));
        Page<org.ganjp.blog.cms.model.dto.AudioResponse> pageResult = audioService.searchAudios(name, lang, tags, isActive, pageable);

        List<PublicAudioResponse> publicList = pageResult.getContent().stream().map(r -> {
            PublicAudioResponse.PublicAudioResponseBuilder b = PublicAudioResponse.builder()
                .id(r.getId())
                .title(r.getName())
                .description(r.getDescription())
                .subtitle(r.getSubtitle())
                .artist(r.getArtist())
                .tags(r.getTags())
                .lang(r.getLang())
                .displayOrder(r.getDisplayOrder())
                .updatedAt(r.getUpdatedAt());

            String fname = r.getFilename();
            b.url(joinBaseAndPath(audioBaseUrl, fname));

            String cimg = r.getCoverImageFilename();
            b.coverImageUrl(joinBasePathWithSegment(audioBaseUrl, "cover-images", cimg));

            return b.build();
        }).toList();

        return PaginatedResponse.of(publicList, page, size, pageResult.getTotalElements());
    }

    public PaginatedResponse<PublicArticleResponse> getArticles(String title, org.ganjp.blog.cms.model.entity.Article.Language lang, String tags, Boolean isActive, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "displayOrder"));
        Page<ArticleResponse> pageResult = articleService.searchArticles(title, lang, tags, isActive, pageable);

        // Map internal ArticleResponse -> PublicArticleResponse and compute coverImageUrl
        List<PublicArticleResponse> publicList = pageResult.getContent().stream().map(r -> {
            PublicArticleResponse.PublicArticleResponseBuilder b = PublicArticleResponse.builder()
                .id(r.getId())
                .title(r.getTitle())
                .summary(r.getSummary())
                .originalUrl(r.getOriginalUrl())
                .sourceName(r.getSourceName())
                .coverImageOriginalUrl(r.getCoverImageOriginalUrl())
                .tags(r.getTags())
                .lang(r.getLang())
                .displayOrder(r.getDisplayOrder())
                .updatedAt(r.getUpdatedAt());

            String cimg = r.getCoverImageFilename();
            b.coverImageUrl(joinBaseAndPath(articleProperties.getCoverImage().getBaseUrl(), cimg));

            return b.build();
        }).toList();

        return PaginatedResponse.of(publicList, page, size, pageResult.getTotalElements());
    }

    public PublicArticleDetailResponse getArticleById(String id) {
        ArticleResponse r = articleService.getArticleById(id);
        if (r == null) return null;

        PublicArticleDetailResponse.PublicArticleDetailResponseBuilder b = PublicArticleDetailResponse.builder()
            .id(r.getId())
            .title(r.getTitle())
            .summary(r.getSummary())
            .content(r.getContent())
            .originalUrl(r.getOriginalUrl())
            .sourceName(r.getSourceName())
            .coverImageOriginalUrl(r.getCoverImageOriginalUrl())
            .tags(r.getTags())
            .lang(r.getLang())
            .displayOrder(r.getDisplayOrder())
            .updatedAt(r.getUpdatedAt());

        // Build coverImageUrl from coverImageFilename using configured base if available
        String cimg = r.getCoverImageFilename();
        if (cimg != null && !cimg.isBlank()) {
            b.coverImageUrl(joinBaseAndPath(articleProperties.getCoverImage().getBaseUrl(), cimg));
        }

        return b.build();
    }

    public PaginatedResponse<org.ganjp.blog.cms.model.dto.QuestionResponse> getQuestions(String question, Question.Language lang, String tags, Boolean isActive, int page, int size) {
        var pageable = PageRequest.of(Math.max(0, page), Math.max(1, size));
        var pageResult = questionService.getQuestions(question, lang, tags, isActive, pageable);
        return PaginatedResponse.of(pageResult.getContent(), pageResult.getNumber(), pageResult.getSize(), pageResult.getTotalElements());
    }
}
