package org.ganjp.blog.rubi.repository;

import org.ganjp.blog.rubi.model.entity.ArticleImageRu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ArticleImageRuRepository extends JpaRepository<ArticleImageRu, String> {
    List<ArticleImageRu> findByArticleRuIdAndIsActiveTrueOrderByDisplayOrderAsc(String articleRuId);

    Optional<ArticleImageRu> findByIdAndIsActiveTrue(String id);

    boolean existsByFilename(String filename);

    @Query("SELECT i FROM ArticleImageRu i WHERE " +
        "(:articleRuId IS NULL OR i.articleRuId = :articleRuId) AND " +
        "(:lang IS NULL OR i.lang = :lang) AND " +
        "(:isActive IS NULL OR i.isActive = :isActive) " +
        "ORDER BY i.displayOrder")
    List<ArticleImageRu> searchArticleImages(@Param("articleRuId") String articleRuId,
                             @Param("lang") org.ganjp.blog.rubi.model.entity.ArticleImageRu.Language lang,
                             @Param("isActive") Boolean isActive);
}
