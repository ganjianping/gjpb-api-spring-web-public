package org.ganjp.blog.rubi.repository;

import org.ganjp.blog.rubi.model.entity.QuestionImageRu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionImageRuRepository extends JpaRepository<QuestionImageRu, String> {
    List<QuestionImageRu> findByMcqIdAndIsActiveTrueOrderByDisplayOrderAsc(String mcqId);

    List<QuestionImageRu> findBySaqIdAndIsActiveTrueOrderByDisplayOrderAsc(String saqId);

    Optional<QuestionImageRu> findByIdAndIsActiveTrue(String id);

    boolean existsByFilename(String filename);

    @Query("SELECT i FROM QuestionImageRu i WHERE " +
        "(:mcqId IS NULL OR i.mcqId = :mcqId) AND " +
        "(:saqId IS NULL OR i.saqId = :saqId) AND " +
        "(:lang IS NULL OR i.lang = :lang) AND " +
        "(:isActive IS NULL OR i.isActive = :isActive) " +
        "ORDER BY i.displayOrder")
    List<QuestionImageRu> searchQuestionImageRus(
        @Param("mcqId") String mcqId,
        @Param("saqId") String saqId,
        @Param("lang") QuestionImageRu.Language lang,
        @Param("isActive") Boolean isActive
    );
}
