package org.ganjp.blog.cms.repository;

import org.ganjp.blog.cms.model.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VideoRepository extends JpaRepository<Video, String> {
    Optional<Video> findByIdAndIsActiveTrue(String id);

    @Query("SELECT v FROM Video v WHERE " +
            "(:name IS NULL OR LOWER(v.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:lang IS NULL OR v.lang = :lang) AND " +
            "(:tags IS NULL OR v.tags LIKE CONCAT('%', :tags, '%')) AND " +
            "(:isActive IS NULL OR v.isActive = :isActive) " +
            "ORDER BY v.displayOrder")
    List<Video> searchVideos(@Param("name") String name,
                             @Param("lang") Video.Language lang,
                             @Param("tags") String tags,
                             @Param("isActive") Boolean isActive);

    boolean existsByFilename(String filename);
}
