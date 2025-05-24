package org.ganjp.blog.am.repository;

import org.ganjp.blog.am.model.entity.User;
import org.ganjp.blog.am.model.enums.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByVerificationToken(String token);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    List<User> findByAccountStatus(AccountStatus accountStatus);
    
    @Query("SELECT u FROM User u WHERE u.accountLockedUntil IS NOT NULL AND u.accountLockedUntil <= :now")
    List<User> findUsersWithExpiredLocks(@Param("now") LocalDateTime now);
    
    @Query("SELECT u FROM User u WHERE u.verificationTokenExpiresAt IS NOT NULL AND u.verificationTokenExpiresAt <= :now AND u.verifiedAt IS NULL")
    List<User> findUsersWithExpiredVerificationTokens(@Param("now") LocalDateTime now);
    
    @Query("SELECT u FROM User u WHERE u.passwordChangedAt IS NOT NULL AND u.passwordChangedAt <= :now")
    List<User> findUsersWithOldPasswords(@Param("now") LocalDateTime now);
}