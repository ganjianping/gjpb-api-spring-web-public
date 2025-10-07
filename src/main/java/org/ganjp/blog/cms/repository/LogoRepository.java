package org.ganjp.blog.cms.repository;

import org.ganjp.blog.cms.model.entity.Logo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Logo entity
 */
@Repository
public interface LogoRepository extends JpaRepository<Logo, String> {

    /**
     * Find all active logos ordered by display order
     */
    List<Logo> findByIsActiveTrueOrderByDisplayOrderAsc();

    /**
     * Find logo by ID and active status
     */
    Optional<Logo> findByIdAndIsActiveTrue(String id);

    /**
     * Find logos by name containing keyword (case insensitive)
     */
    @Query("SELECT l FROM Logo l WHERE LOWER(l.name) LIKE LOWER(CONCAT('%', :keyword, '%')) AND l.isActive = true ORDER BY l.displayOrder")
    List<Logo> searchByNameContaining(@Param("keyword") String keyword);

    /**
     * Find logos by tag
     */
    @Query("SELECT l FROM Logo l WHERE l.tags LIKE CONCAT('%', :tag, '%') AND l.isActive = true ORDER BY l.displayOrder")
    List<Logo> findByTagsContaining(@Param("tag") String tag);

    /**
     * Check if logo with filename exists
     */
    boolean existsByFilename(String filename);
}
