package org.ganjp.blog.rubi.repository;

import org.ganjp.blog.rubi.model.entity.ImageRu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ImageRuRepository extends JpaRepository<ImageRu, String> {
    @Query("SELECT i FROM ImageRu i WHERE LOWER(i.name) LIKE LOWER(CONCAT('%', :keyword, '%')) AND i.isActive = true")
    Page<ImageRu> searchByNameContaining(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT i FROM ImageRu i WHERE i.tags LIKE CONCAT('%', :tag, '%') AND i.isActive = true ORDER BY i.displayOrder")
    List<ImageRu> findByTagsContaining(@Param("tag") String tag);

    @Query("SELECT i FROM ImageRu i WHERE " +
        "(:name IS NULL OR LOWER(i.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
        "(:lang IS NULL OR i.lang = :lang) AND " +
        "(:tags IS NULL OR i.tags LIKE CONCAT('%', :tags, '%')) AND " +
        "(:isActive IS NULL OR i.isActive = :isActive)")
    Page<ImageRu> searchImages(@Param("name") String name,
                             @Param("lang") org.ganjp.blog.rubi.model.entity.ImageRu.Language lang,
                             @Param("tags") String tags,
                             @Param("isActive") Boolean isActive,
                             Pageable pageable);

    List<ImageRu> findByIsActiveTrueOrderByDisplayOrderAsc();

    Optional<ImageRu> findByIdAndIsActiveTrue(String id);

    boolean existsByFilename(String filename);
    boolean existsByThumbnailFilename(String thumbnailFilename);

    @Query("SELECT i FROM ImageRu i WHERE (i.filename = :name OR i.thumbnailFilename = :name) AND i.isActive = true")
    Optional<ImageRu> findByFilenameAndIsActiveTrue(@Param("name") String name);
}
