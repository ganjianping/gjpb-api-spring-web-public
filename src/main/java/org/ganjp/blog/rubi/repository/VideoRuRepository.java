package org.ganjp.blog.rubi.repository;

import org.ganjp.blog.rubi.model.entity.VideoRu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VideoRuRepository extends JpaRepository<VideoRu, String> {
    Optional<VideoRu> findByIdAndIsActiveTrue(String id);

    @Query("SELECT v FROM VideoRu v WHERE " +
            "(:name IS NULL OR LOWER(v.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:lang IS NULL OR v.lang = :lang) AND " +
            "(:tags IS NULL OR v.tags LIKE CONCAT('%', :tags, '%')) AND " +
            "(:isActive IS NULL OR v.isActive = :isActive)")
    Page<VideoRu> searchVideos(@Param("name") String name,
                             @Param("lang") VideoRu.Language lang,
                             @Param("tags") String tags,
                             @Param("isActive") Boolean isActive,
                             Pageable pageable);

    @Query("SELECT v FROM VideoRu v WHERE " +
            "(:name IS NULL OR LOWER(v.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:lang IS NULL OR v.lang = :lang) AND " +
            "(:tags IS NULL OR v.tags LIKE CONCAT('%', :tags, '%')) AND " +
            "(:isActive IS NULL OR v.isActive = :isActive) " +
            "ORDER BY v.displayOrder")
    List<VideoRu> searchVideos(@Param("name") String name,
                             @Param("lang") VideoRu.Language lang,
                             @Param("tags") String tags,
                             @Param("isActive") Boolean isActive);
    boolean existsByFilenameOrCoverImageFilename(String filename, String coverImageFilename);

    default boolean existsByFilename(String filename) {
        return existsByFilenameOrCoverImageFilename(filename, filename);
    }

    Optional<VideoRu> findByFilenameAndIsActiveTrue(String filename);
}
