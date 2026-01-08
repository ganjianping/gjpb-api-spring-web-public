package org.ganjp.blog.rubi.repository;

import org.ganjp.blog.rubi.model.entity.MultipleChoiceQuestionRu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MultipleChoiceQuestionRuRepository extends JpaRepository<MultipleChoiceQuestionRu, String>, JpaSpecificationExecutor<MultipleChoiceQuestionRu> {

    List<MultipleChoiceQuestionRu> findByLangAndIsActiveTrueOrderByDisplayOrderAsc(MultipleChoiceQuestionRu.Language lang);

    List<MultipleChoiceQuestionRu> findByDifficultyLevelAndIsActiveTrue(String difficultyLevel);

    List<MultipleChoiceQuestionRu> findByTagsContainingAndIsActiveTrue(String tag);
}