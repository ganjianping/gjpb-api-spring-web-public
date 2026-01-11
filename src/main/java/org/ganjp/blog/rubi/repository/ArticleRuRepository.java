package org.ganjp.blog.rubi.repository;

import org.ganjp.blog.rubi.model.entity.ArticleRu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ArticleRuRepository extends JpaRepository<ArticleRu, String> {
    Optional<ArticleRu> findByIdAndIsActiveTrue(String id);

    @Query("SELECT a FROM ArticleRu a WHERE " +
        "(:title IS NULL OR LOWER(a.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
        "(:lang IS NULL OR a.lang = :lang) AND " +
        "(:tags IS NULL OR a.tags LIKE CONCAT('%', :tags, '%')) AND " +
        "(:isActive IS NULL OR a.isActive = :isActive)")
    Page<ArticleRu> searchArticles(@Param("title") String title,
                 @Param("lang") org.ganjp.blog.rubi.model.entity.ArticleRu.Language lang,
                 @Param("tags") String tags,
                 @Param("isActive") Boolean isActive,
                 Pageable pageable);

    boolean existsByCoverImageFilename(String filename);

    default boolean existsByFilename(String filename) {
        return existsByCoverImageFilename(filename);
    }
}
