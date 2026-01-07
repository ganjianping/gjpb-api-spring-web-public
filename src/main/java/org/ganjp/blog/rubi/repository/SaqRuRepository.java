package org.ganjp.blog.rubi.repository;

import org.ganjp.blog.rubi.model.entity.SaqRu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SaqRuRepository extends JpaRepository<SaqRu, String>, JpaSpecificationExecutor<SaqRu> {

    List<SaqRu> findByLangAndIsActiveTrueOrderByDisplayOrderAsc(SaqRu.Language lang);

    List<SaqRu> findByDifficultyLevelAndIsActiveTrue(String difficultyLevel);

    List<SaqRu> findByTagsContainingAndIsActiveTrue(String tag);
}