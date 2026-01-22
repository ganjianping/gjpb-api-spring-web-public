package org.ganjp.blog.rubi.repository;

import org.ganjp.blog.rubi.model.entity.ExpressionRu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpressionRuRepository extends JpaRepository<ExpressionRu, String>, JpaSpecificationExecutor<ExpressionRu> {
    
    List<ExpressionRu> findByLangAndIsActiveTrueOrderByDisplayOrderAsc(ExpressionRu.Language lang);
    
    boolean existsByNameAndLang(String name, ExpressionRu.Language lang);
    
    boolean existsByPhoneticAudioFilenameAndIsActiveTrue(String phoneticAudioFilename);
}
