package org.ganjp.blog.rubi.repository;

import org.ganjp.blog.rubi.model.entity.FreeTextQuestionRu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FreeTextQuestionRuRepository extends JpaRepository<FreeTextQuestionRu, String>, JpaSpecificationExecutor<FreeTextQuestionRu> {

    List<FreeTextQuestionRu> findByLangAndIsActiveTrueOrderByDisplayOrderAsc(FreeTextQuestionRu.Language lang);

    List<FreeTextQuestionRu> findByDifficultyLevelAndIsActiveTrue(String difficultyLevel);

    List<FreeTextQuestionRu> findByTagsContainingAndIsActiveTrue(String tag);
}