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
    List<QuestionImageRu> findByMultipleChoiceQuestionIdAndIsActiveTrueOrderByDisplayOrderAsc(String multipleChoiceQuestionId);

    List<QuestionImageRu> findByFreeTextQuestionIdAndIsActiveTrueOrderByDisplayOrderAsc(String freeTextQuestionId);

    List<QuestionImageRu> findByTrueFalseQuestionIdAndIsActiveTrueOrderByDisplayOrderAsc(String trueFalseQuestionId);

    List<QuestionImageRu> findByFillBlankQuestionIdAndIsActiveTrueOrderByDisplayOrderAsc(String fillBlankQuestionId);

    Optional<QuestionImageRu> findByIdAndIsActiveTrue(String id);

    boolean existsByFilename(String filename);

    boolean existsByFilenameAndIsActiveTrue(String filename);

    @Query("SELECT i FROM QuestionImageRu i WHERE " +
        "(:multipleChoiceQuestionId IS NULL OR i.multipleChoiceQuestionId = :multipleChoiceQuestionId) AND " +
        "(:freeTextQuestionId IS NULL OR i.freeTextQuestionId = :freeTextQuestionId) AND " +
        "(:trueFalseQuestionId IS NULL OR i.trueFalseQuestionId = :trueFalseQuestionId) AND " +
        "(:fillBlankQuestionId IS NULL OR i.fillBlankQuestionId = :fillBlankQuestionId) AND " +
        "(:lang IS NULL OR i.lang = :lang) AND " +
        "(:isActive IS NULL OR i.isActive = :isActive) " +
        "ORDER BY i.displayOrder")
    List<QuestionImageRu> searchQuestionImageRus(
        @Param("multipleChoiceQuestionId") String multipleChoiceQuestionId,
        @Param("freeTextQuestionId") String freeTextQuestionId,
        @Param("trueFalseQuestionId") String trueFalseQuestionId,
        @Param("fillBlankQuestionId") String fillBlankQuestionId,
        @Param("lang") QuestionImageRu.Language lang,
        @Param("isActive") Boolean isActive
    );
}
