package org.ganjp.blog.rubi.repository;

import org.ganjp.blog.rubi.model.entity.VocabularyRu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VocabularyRuRepository extends JpaRepository<VocabularyRu, String>, JpaSpecificationExecutor<VocabularyRu> {
    
    List<VocabularyRu> findByLangAndIsActiveTrueOrderByDisplayOrderAsc(VocabularyRu.Language lang);
    
    boolean existsByWordAndLang(String word, VocabularyRu.Language lang);
}
