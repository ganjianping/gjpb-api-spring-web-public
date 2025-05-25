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
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);

    boolean existsByMobileCountryCodeAndMobileNumber(String mobileCountryCode, String mobileNumber);
    
    List<User> findByAccountStatus(AccountStatus accountStatus);
    
    @Query("SELECT u FROM User u WHERE u.accountLockedUntil IS NOT NULL AND u.accountLockedUntil <= :now")
    List<User> findUsersWithExpiredLocks(@Param("now") LocalDateTime now);
    
    @Query("SELECT u FROM User u WHERE u.passwordChangedAt IS NOT NULL AND u.passwordChangedAt <= :now")
    List<User> findUsersWithOldPasswords(@Param("now") LocalDateTime now);
}