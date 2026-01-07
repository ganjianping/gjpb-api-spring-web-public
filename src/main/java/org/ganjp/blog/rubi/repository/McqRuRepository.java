package org.ganjp.blog.rubi.repository;

import org.ganjp.blog.rubi.model.entity.McqRu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface McqRuRepository extends JpaRepository<McqRu, String>, JpaSpecificationExecutor<McqRu> {

    List<McqRu> findByLangAndIsActiveTrueOrderByDisplayOrderAsc(McqRu.Language lang);

    List<McqRu> findByDifficultyLevelAndIsActiveTrue(String difficultyLevel);

    List<McqRu> findByTagsContainingAndIsActiveTrue(String tag);
}