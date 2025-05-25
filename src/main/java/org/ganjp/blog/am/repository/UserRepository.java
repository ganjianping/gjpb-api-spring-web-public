package org.ganjp.blog.am.repository;

import org.ganjp.blog.am.model.entity.User;
import org.ganjp.blog.am.model.enums.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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
    
    Optional<User> findByMobileCountryCodeAndMobileNumber(String mobileCountryCode, String mobileNumber);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);

    boolean existsByMobileCountryCodeAndMobileNumber(String mobileCountryCode, String mobileNumber);
    
    List<User> findByAccountStatus(AccountStatus accountStatus);
    
    @Modifying
    @Query("UPDATE User u SET u.lastLoginAt = :now, u.lastLoginIp = :ip WHERE u.id = :userId")
    void updateLoginSuccess(@Param("userId") String userId, @Param("now") LocalDateTime now, @Param("ip") String ip);
    
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE User u SET u.failedLoginAttempts = u.failedLoginAttempts + 1, u.lastFailedLoginAt = :now WHERE " +
           "(:username IS NOT NULL AND u.username = :username) OR " +
           "(:email IS NOT NULL AND u.email = :email) OR " +
           "(:mobileCountryCode IS NOT NULL AND :mobileNumber IS NOT NULL AND u.mobileCountryCode = :mobileCountryCode AND u.mobileNumber = :mobileNumber)")
    int updateLoginFailure(@Param("username") String username, @Param("email") String email, 
                           @Param("mobileCountryCode") String mobileCountryCode, @Param("mobileNumber") String mobileNumber, 
                           @Param("now") LocalDateTime now);
    
    @Query("SELECT u FROM User u WHERE u.accountLockedUntil IS NOT NULL AND u.accountLockedUntil <= :now")
    List<User> findUsersWithExpiredLocks(@Param("now") LocalDateTime now);
    
    @Query("SELECT u FROM User u WHERE u.passwordChangedAt IS NOT NULL AND u.passwordChangedAt <= :now")
    List<User> findUsersWithOldPasswords(@Param("now") LocalDateTime now);
}