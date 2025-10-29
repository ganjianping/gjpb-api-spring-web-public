package org.ganjp.blog.open.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ganjp.blog.cms.model.dto.*;
import org.ganjp.blog.cms.model.entity.Website;
import org.ganjp.blog.cms.service.*;
import org.ganjp.blog.open.model.PaginatedResponse;
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
    private final VideoService videoService;
    private final org.ganjp.blog.cms.service.FileService fileService;
    private final AudioService audioService;
    private final ArticleService articleService;

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

    public PaginatedResponse<ImageResponse> getImages(String name, org.ganjp.blog.cms.model.entity.Image.Language lang, String tags, Boolean isActive, int page, int size) {
        List<ImageResponse> all = imageService.searchImages(name, lang, tags, isActive);
        return paginateList(all, page, size);
    }

    public PaginatedResponse<ImageResponse> getLogos(String name, org.ganjp.blog.cms.model.entity.Image.Language lang, String tags, Boolean isActive, int page, int size) {
        // logos are images but processed differently; reuse imageService search for now
        List<ImageResponse> all = imageService.searchImages(name, lang, tags, isActive);
        return paginateList(all, page, size);
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

    public PaginatedResponse<ArticleResponse> getArticles(String title, org.ganjp.blog.cms.model.entity.Article.Language lang, String tags, Boolean isActive, int page, int size) {
        List<ArticleResponse> all = articleService.searchArticles(title, lang, tags, isActive);
        return paginateList(all, page, size);
    }
}
