package org.ganjp.blog.rubi.repository;

import org.ganjp.blog.rubi.model.entity.TrueFalseQuestionRu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrueFalseQuestionRuRepository extends JpaRepository<TrueFalseQuestionRu, String>, JpaSpecificationExecutor<TrueFalseQuestionRu> {

    List<TrueFalseQuestionRu> findByLangAndIsActiveTrueOrderByDisplayOrderAsc(TrueFalseQuestionRu.Language lang);

    List<TrueFalseQuestionRu> findByDifficultyLevelAndIsActiveTrue(String difficultyLevel);

    List<TrueFalseQuestionRu> findByTagsContainingAndIsActiveTrue(String tag);
}
