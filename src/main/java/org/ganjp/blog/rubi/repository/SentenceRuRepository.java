package org.ganjp.blog.rubi.repository;

import org.ganjp.blog.rubi.model.entity.SentenceRu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SentenceRuRepository extends JpaRepository<SentenceRu, String>, JpaSpecificationExecutor<SentenceRu> {
    
    List<SentenceRu> findByLangAndIsActiveTrueOrderByDisplayOrderAsc(SentenceRu.Language lang);
    
    boolean existsByNameAndLang(String name, SentenceRu.Language lang);
    
    boolean existsByPhoneticAudioFilenameAndIsActiveTrue(String phoneticAudioFilename);
}
