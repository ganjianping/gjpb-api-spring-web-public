package org.ganjp.blog.cms.repository;

import org.ganjp.blog.cms.model.entity.File;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FileRepository extends JpaRepository<File, String> {
    Optional<File> findByIdAndIsActiveTrue(String id);

    @Query("SELECT f FROM File f WHERE " +
            "(:name IS NULL OR LOWER(f.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:lang IS NULL OR f.lang = :lang) AND " +
            "(:tags IS NULL OR f.tags LIKE CONCAT('%', :tags, '%')) AND " +
            "(:isActive IS NULL OR f.isActive = :isActive) " +
            "ORDER BY f.displayOrder")
    List<File> searchFiles(@Param("name") String name,
                           @Param("lang") File.Language lang,
                           @Param("tags") String tags,
                           @Param("isActive") Boolean isActive);

    boolean existsByFilename(String filename);
}
