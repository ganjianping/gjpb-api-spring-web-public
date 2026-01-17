package org.ganjp.blog.open.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ganjp.blog.common.model.ApiResponse;
import org.ganjp.blog.common.model.PaginatedResponse;
import org.ganjp.blog.open.model.PublicVocabularyRuResponse;
import org.ganjp.blog.open.service.PublicRubiService;
import org.ganjp.blog.rubi.model.entity.VocabularyRu;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

@RestController
@RequestMapping("/v1/public")
@RequiredArgsConstructor
@Slf4j
public class PublicRubiController {
    private final PublicRubiService publicRubiService;

    @GetMapping("/vocabulary-rus")
    public ApiResponse<PaginatedResponse<PublicVocabularyRuResponse>> getVocabularies(@RequestParam(required = false) String name,
                                                             @RequestParam(required = false) String lang,
                                                             @RequestParam(required = false) String tags,
                                                             @RequestParam(required = false) Integer term,
                                                             @RequestParam(required = false) Integer week,
                                                             @RequestParam(required = false) String difficultyLevel,
                                                             @RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "20") int size,
                                                             @RequestParam(defaultValue = "displayOrder") String sort,
                                                             @RequestParam(defaultValue = "asc") String direction) {
        VocabularyRu.Language langEnum = null;
        if (lang != null && !lang.isBlank()) {
            try {
                langEnum = VocabularyRu.Language.valueOf(lang.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ex) {
                return ApiResponse.error(400, "Invalid lang", null);
            }
        }
        var resp = publicRubiService.getVocabularies(name, langEnum, tags, term, week, difficultyLevel, page, size);
        return ApiResponse.success(resp, "Vocabularies retrieved");
    }
}
