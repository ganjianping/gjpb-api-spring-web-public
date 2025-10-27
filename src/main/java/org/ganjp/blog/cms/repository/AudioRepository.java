package org.ganjp.blog.cms.repository;

import org.ganjp.blog.cms.model.entity.Audio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AudioRepository extends JpaRepository<Audio, String> {
    Optional<Audio> findByIdAndIsActiveTrue(String id);

    @Query("SELECT a FROM Audio a WHERE " +
        "(:name IS NULL OR LOWER(a.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
        "(:lang IS NULL OR a.lang = :lang) AND " +
        "(:tags IS NULL OR a.tags LIKE CONCAT('%', :tags, '%')) AND " +
        "(:isActive IS NULL OR a.isActive = :isActive) " +
        "ORDER BY a.displayOrder")
    List<Audio> searchAudios(@Param("name") String name,
                 @Param("lang") Audio.Language lang,
                 @Param("tags") String tags,
                 @Param("isActive") Boolean isActive);

    boolean existsByFilenameOrCoverImageFilename(String filename, String coverImageFilename);

    default boolean existsByFilename(String filename) {
        return existsByFilenameOrCoverImageFilename(filename, filename);
    }
}
