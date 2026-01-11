package org.ganjp.blog.rubi.repository;

import org.ganjp.blog.rubi.model.entity.FillBlankQuestionRu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FillBlankQuestionRuRepository extends JpaRepository<FillBlankQuestionRu, String>, JpaSpecificationExecutor<FillBlankQuestionRu> {

    List<FillBlankQuestionRu> findByLangAndIsActiveTrueOrderByDisplayOrderAsc(FillBlankQuestionRu.Language lang);

    List<FillBlankQuestionRu> findByDifficultyLevelAndIsActiveTrue(String difficultyLevel);

    List<FillBlankQuestionRu> findByTagsContainingAndIsActiveTrue(String tag);
}
