package org.ganjp.blog.am.repository;

import org.ganjp.blog.am.model.entity.Role;
import org.ganjp.blog.am.model.entity.User;
import org.ganjp.blog.am.model.entity.UserRole;
import org.ganjp.blog.am.model.entity.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {
    
    List<UserRole> findByUserAndActiveTrue(User user);
    
    List<UserRole> findByRoleAndActiveTrue(Role role);
    
    Optional<UserRole> findByUserAndRole(User user, Role role);
    
    boolean existsByUserAndRole(User user, Role role);
    
    @Query("SELECT ur FROM UserRole ur WHERE ur.user = :user AND ur.active = true AND (ur.expiresAt IS NULL OR ur.expiresAt > :now)")
    List<UserRole> findActiveUserRoles(@Param("user") User user, @Param("now") LocalDateTime now);
    
    @Query("SELECT ur FROM UserRole ur WHERE ur.expiresAt IS NOT NULL AND ur.expiresAt <= :now AND ur.active = true")
    List<UserRole> findExpiredUserRoles(@Param("now") LocalDateTime now);
    
    @Query("SELECT COUNT(ur) FROM UserRole ur WHERE ur.role = :role AND ur.active = true")
    long countActiveUsersWithRole(@Param("role") Role role);
}
