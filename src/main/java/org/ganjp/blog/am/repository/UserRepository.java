package org.ganjp.blog.am.repository;

import org.ganjp.blog.am.model.entity.User;
import org.ganjp.blog.am.model.enums.AccountStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUsername(String username);
    
    Page<User> findByUsernameContainingIgnoreCase(String username, Pageable pageable);
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByMobileCountryCodeAndMobileNumber(String mobileCountryCode, String mobileNumber);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);

    boolean existsByMobileCountryCodeAndMobileNumber(String mobileCountryCode, String mobileNumber);
    
    List<User> findByAccountStatus(AccountStatus accountStatus);
    
    @Modifying
    @Query("UPDATE User u SET u.lastLoginAt = :now, u.lastLoginIp = :ip WHERE u.id = :userId")
    void updateLoginSuccess(@Param("userId") String userId, @Param("now") LocalDateTime now, @Param("ip") String ip);

    // get username by email or mobile country code + mobile number
    @Query("SELECT u.username FROM User u WHERE u.email = :email OR (u.mobileCountryCode = :mobileCountryCode AND u.mobileNumber = :mobileNumber)")
    Optional<String> findUsernameByEmailOrMobile(@Param("email") String email,
                                                  @Param("mobileCountryCode") String mobileCountryCode,
                                                  @Param("mobileNumber") String mobileNumber);
    
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Query(value = "UPDATE auth_users SET failed_login_attempts = COALESCE(failed_login_attempts, 0) + 1, last_failed_login_at = :now WHERE id = :userId", nativeQuery = true)
    int updateLoginFailureByIdNative(@Param("userId") String userId, @Param("now") LocalDateTime now);

    @Query("SELECT u FROM User u WHERE u.accountLockedUntil IS NOT NULL AND u.accountLockedUntil <= :now")
    List<User> findUsersWithExpiredLocks(@Param("now") LocalDateTime now);
    
    @Query("SELECT u FROM User u WHERE u.passwordChangedAt IS NOT NULL AND u.passwordChangedAt <= :now")
    List<User> findUsersWithOldPasswords(@Param("now") LocalDateTime now);
    
    /**
     * Find users who have a specific role assigned
     * @param roleCode The role code to search for
     * @param pageable Pagination information
     * @return Page of users with the specified role
     */
    @Query("SELECT DISTINCT u FROM User u " +
           "JOIN UserRole ur ON u.id = ur.user.id " +
           "JOIN Role r ON ur.role.id = r.id " +
           "WHERE r.code = :roleCode " +
           "AND ur.active = true " +
           "AND (ur.expiresAt IS NULL OR ur.expiresAt > CURRENT_TIMESTAMP)")
    Page<User> findUsersByRoleCode(@Param("roleCode") String roleCode, Pageable pageable);
    
    /**
     * Find users who have a specific role assigned and username contains the search term
     * @param roleCode The role code to search for
     * @param username Username substring to search for
     * @param pageable Pagination information
     * @return Page of users with the specified role and matching username
     */
    @Query("SELECT DISTINCT u FROM User u " +
           "JOIN UserRole ur ON u.id = ur.user.id " +
           "JOIN Role r ON ur.role.id = r.id " +
           "WHERE r.code = :roleCode " +
           "AND ur.active = true " +
           "AND (ur.expiresAt IS NULL OR ur.expiresAt > CURRENT_TIMESTAMP) " +
           "AND LOWER(u.username) LIKE LOWER(CONCAT('%', :username, '%'))")
    Page<User> findUsersByRoleCodeAndUsernameContaining(@Param("roleCode") String roleCode, 
                                                        @Param("username") String username, 
                                                        Pageable pageable);
}