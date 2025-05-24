package org.ganjp.blog.am.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ganjp.blog.am.model.enums.AccountStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "auth_users")
public class User implements UserDetails {

    @Id
    @Column(columnDefinition = "CHAR(36)")
    private String id;
    
    @Column(length = 50, unique = true, nullable = false)
    private String username;
    
    @Column(length = 128, unique = true, nullable = false)
    private String email;
    
    @Column(name = "password_hash")
    private String password;
    
    @Column(name = "first_name", length = 50)
    private String firstName;
    
    @Column(name = "last_name", length = 50)
    private String lastName;
    
    // Account status management
    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false)
    @Builder.Default
    private AccountStatus accountStatus = AccountStatus.pending_verification;
    
    @Column(name = "account_locked_until")
    private LocalDateTime accountLockedUntil;
    
    // Email / SMS Verification
    @Column(name = "verification_token", length = 128)
    private String verificationToken;
    
    @Column(name = "verification_token_expires_at")
    private LocalDateTime verificationTokenExpiresAt;
    
    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;
    
    // Multi-factor authentication
    @Column(name = "mfa_enabled", nullable = false)
    @Builder.Default
    private boolean mfaEnabled = false;
    
    @Column(name = "mfa_secret", length = 255)
    private String mfaSecret;
    
    @Column(name = "mfa_last_used_at")
    private LocalDateTime mfaLastUsedAt;
    
    // Login tracking
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;
    
    @Column(name = "last_login_ip", length = 45)
    private String lastLoginIp;
    
    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;
    
    @Column(name = "failed_login_attempts", nullable = false)
    @Builder.Default
    private int failedLoginAttempts = 0;
    
    @Column(name = "last_failed_login_at")
    private LocalDateTime lastFailedLoginAt;
    
    // Audit fields
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "created_by", columnDefinition = "CHAR(36)")
    private String createdBy;
    
    @Column(name = "updated_by", columnDefinition = "CHAR(36)")
    private String updatedBy;
    
    // Soft delete
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;
    
    // For UI display purposes (non-persistent)
    @Transient
    @Builder.Default
    private Integer displayOrder = 0;

    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @Builder.Default
    private List<UserRole> userRoles = new ArrayList<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (userRoles != null) {
            for (UserRole userRole : userRoles) {
                if (userRole.isActive() && (userRole.getExpiresAt() == null || userRole.getExpiresAt().isAfter(LocalDateTime.now()))) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + userRole.getRole().getCode()));
                }
            }
        }
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountStatus != AccountStatus.suspended && active;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountStatus != AccountStatus.locked 
            && (accountLockedUntil == null || accountLockedUntil.isBefore(LocalDateTime.now()));
    }

    @Override
    public boolean isCredentialsNonExpired() {
        // Check if we need to force a password reset
        // passwordChangedAt should not be null as it has a default value in DB
        return passwordChangedAt != null;
    }

    @Override
    public boolean isEnabled() {
        return accountStatus == AccountStatus.active && verifiedAt != null && active;
    }
    
    /**
     * Get active roles (non-expired and active)
     */
    public List<Role> getRoles() {
        if (userRoles == null) {
            return new ArrayList<>();
        }
        return userRoles.stream()
                .filter(ur -> ur.isActive() && (ur.getExpiresAt() == null || ur.getExpiresAt().isAfter(LocalDateTime.now())))
                .map(UserRole::getRole)
                .toList();
    }
    
    /**
     * Check if user is active (active flag and specific account status)
     */
    public boolean isActive() {
        return active && accountStatus == AccountStatus.active;
    }

    /**
     * Builder methods for compatibility with tests
     */
    public static class UserBuilder {
        public UserBuilder emailVerificationToken(String token) {
            this.verificationToken = token;
            return this;
        }
        
        public UserBuilder emailVerificationExpiresAt(LocalDateTime expiresAt) {
            this.verificationTokenExpiresAt = expiresAt;
            return this;
        }
        
        public UserBuilder passwordLastChangedAt(LocalDateTime changedAt) {
            this.passwordChangedAt = changedAt;
            return this;
        }
    }
}