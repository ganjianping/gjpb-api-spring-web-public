package org.ganjp.blog.am.repository;

import org.ganjp.blog.am.model.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, String> {
    Optional<Role> findByCode(String code);
    
    List<Role> findByActiveTrue();
    
    boolean existsByCode(String code);
}