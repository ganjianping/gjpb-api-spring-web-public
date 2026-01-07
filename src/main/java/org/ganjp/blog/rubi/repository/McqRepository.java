package org.ganjp.blog.rubi.repository;

import org.ganjp.blog.rubi.model.entity.Mcq;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface McqRepository extends JpaRepository<Mcq, String>, JpaSpecificationExecutor<Mcq> {

    List<Mcq> findByLangAndIsActiveTrueOrderByDisplayOrderAsc(Mcq.Language lang);

    List<Mcq> findByDifficultyLevelAndIsActiveTrue(String difficultyLevel);

    List<Mcq> findByTagsContainingAndIsActiveTrue(String tag);
}