package org.ganjp.blog.rubi.repository;

import org.ganjp.blog.rubi.model.entity.Vocabulary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VocabularyRepository extends JpaRepository<Vocabulary, String>, JpaSpecificationExecutor<Vocabulary> {
    
    List<Vocabulary> findByLangAndIsActiveTrueOrderByDisplayOrderAsc(Vocabulary.Language lang);
    
    boolean existsByWordAndLang(String word, Vocabulary.Language lang);
}
