package org.ganjp.blog.rubi.repository;

import org.ganjp.blog.rubi.model.entity.Saq;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SaqRepository extends JpaRepository<Saq, String>, JpaSpecificationExecutor<Saq> {

    List<Saq> findByLangAndIsActiveTrueOrderByDisplayOrderAsc(Saq.Language lang);

    List<Saq> findByDifficultyLevelAndIsActiveTrue(String difficultyLevel);

    List<Saq> findByTagsContainingAndIsActiveTrue(String tag);
}