package org.ganjp.blog.open.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ganjp.blog.cms.model.dto.*;
import org.ganjp.blog.cms.model.entity.Image;
import org.ganjp.blog.cms.model.entity.Website;
import org.ganjp.blog.cms.service.*;
import org.ganjp.blog.cms.service.LogoService;
import org.ganjp.blog.cms.model.dto.LogoResponse;
import org.ganjp.blog.open.model.PublicLogoResponse;
import org.ganjp.blog.open.model.PaginatedResponse;
import org.ganjp.blog.open.model.PublicArticleDetailResponse;
import org.ganjp.blog.open.model.PublicArticleResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
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
    @Value("${article.cover-image-base-url:}")
    private String articleCoverImageBaseUrl;
    @Value("${image.base-url:}")
    private String imageBaseUrl;
    @Value("${logo.base-url:}")
    private String logoBaseUrl;

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

    public PaginatedResponse<org.ganjp.blog.open.model.PublicImageResponse> getImages(String name, Image.Language lang, String tags, Boolean isActive, int page, int size) {
        List<ImageResponse> all = imageService.searchImages(name, lang, tags, isActive);

        // Map internal ImageResponse -> PublicImageResponse and compute urls
        List<org.ganjp.blog.open.model.PublicImageResponse> publicList = all.stream().map(r -> {
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
            if (fname != null && !fname.isBlank()) {
                if (imageBaseUrl != null && !imageBaseUrl.isBlank()) {
                    String prefix = imageBaseUrl;
                    if (!prefix.endsWith("/") && !fname.startsWith("/")) prefix = prefix + "/";
                    else if (prefix.endsWith("/") && fname.startsWith("/")) fname = fname.substring(1);
                    b.url(prefix + fname);
                } else if (fname.startsWith("http") || fname.startsWith("/")) {
                    b.url(fname);
                }
            }

            String tname = r.getThumbnailFilename();
            if (tname != null && !tname.isBlank()) {
                if (imageBaseUrl != null && !imageBaseUrl.isBlank()) {
                    String prefix = imageBaseUrl;
                    if (!prefix.endsWith("/") && !tname.startsWith("/")) prefix = prefix + "/";
                    else if (prefix.endsWith("/") && tname.startsWith("/")) tname = tname.substring(1);
                    b.thumbnailUrl(prefix + tname);
                } else if (tname.startsWith("http") || tname.startsWith("/")) {
                    b.thumbnailUrl(tname);
                }
            }

            return b.build();
        }).toList();

        return paginateList(publicList, page, size);
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
            if (fname != null && !fname.isBlank()) {
                if (logoBaseUrl != null && !logoBaseUrl.isBlank()) {
                    String prefix = logoBaseUrl;
                    if (!prefix.endsWith("/") && !fname.startsWith("/")) prefix = prefix + "/";
                    else if (prefix.endsWith("/") && fname.startsWith("/")) fname = fname.substring(1);
                    b.url(prefix + fname);
                    // no separate thumbnail stored; reuse same URL for thumbnail by default
                    b.thumbnailUrl(prefix + fname);
                } else if (fname.startsWith("http") || fname.startsWith("/")) {
                    b.url(fname);
                    b.thumbnailUrl(fname);
                }
            }

            return b.build();
        }).toList();

        return paginateList(publicList, page, size);
    }

    public PaginatedResponse<org.ganjp.blog.cms.model.dto.VideoResponse> getVideos(String name, org.ganjp.blog.cms.model.entity.Video.Language lang, String tags, Boolean isActive, int page, int size) {
        List<org.ganjp.blog.cms.model.dto.VideoResponse> all = videoService.searchVideos(name, lang, tags, isActive);
        return paginateList(all, page, size);
    }

    public PaginatedResponse<org.ganjp.blog.cms.model.dto.FileResponse> getFiles(String name, org.ganjp.blog.cms.model.entity.File.Language lang, String tags, Boolean isActive, int page, int size) {
        List<org.ganjp.blog.cms.model.dto.FileResponse> all = fileService.searchFiles(name, lang, tags, isActive);
        return paginateList(all, page, size);
    }

    public PaginatedResponse<org.ganjp.blog.cms.model.dto.AudioResponse> getAudios(String name, org.ganjp.blog.cms.model.entity.Audio.Language lang, String tags, Boolean isActive, int page, int size) {
        List<org.ganjp.blog.cms.model.dto.AudioResponse> all = audioService.searchAudios(name, lang, tags, isActive);
        return paginateList(all, page, size);
    }

    public PaginatedResponse<PublicArticleResponse> getArticles(String title, org.ganjp.blog.cms.model.entity.Article.Language lang, String tags, Boolean isActive, int page, int size) {
        List<ArticleResponse> allInternal = articleService.searchArticles(title, lang, tags, isActive);

        // Map internal ArticleResponse -> PublicArticleResponse and compute coverImageUrl
        List<PublicArticleResponse> publicList = allInternal.stream().map(r -> {
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
            if (cimg != null && !cimg.isBlank()) {
                if (articleCoverImageBaseUrl != null && !articleCoverImageBaseUrl.isBlank()) {
                    String prefix = articleCoverImageBaseUrl;
                    if (!prefix.endsWith("/") && !cimg.startsWith("/")) prefix = prefix + "/";
                    else if (prefix.endsWith("/") && cimg.startsWith("/")) cimg = cimg.substring(1);
                    b.coverImageUrl(prefix + cimg);
                } else if (cimg.startsWith("http") || cimg.startsWith("/")) {
                    b.coverImageUrl(cimg);
                }
            }

            return b.build();
        }).toList();

        return paginateList(publicList, page, size);
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
            if (articleCoverImageBaseUrl != null && !articleCoverImageBaseUrl.isBlank()) {
                String prefix = articleCoverImageBaseUrl;
                if (!prefix.endsWith("/") && !cimg.startsWith("/")) prefix = prefix + "/";
                else if (prefix.endsWith("/") && cimg.startsWith("/")) cimg = cimg.substring(1);
                b.coverImageUrl(prefix + cimg);
            } else if (cimg.startsWith("http") || cimg.startsWith("/")) {
                b.coverImageUrl(cimg);
            }
        }

        return b.build();
    }
}
