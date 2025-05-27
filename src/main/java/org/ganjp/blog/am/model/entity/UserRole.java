package org.ganjp.blog.am.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing the relationship between users and roles with enhanced tracking.
 * Maps to the auth_user_roles table.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "auth_user_roles")
@IdClass(UserRoleId.class)
public class UserRole {
    
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", columnDefinition = "CHAR(36)")
    private User user;
    
    @Id
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", columnDefinition = "CHAR(36)")
    private Role role;

    // Role assignment tracking
    @Column(name = "granted_at", nullable = false)
    private LocalDateTime grantedAt;
    
    @Column(name = "granted_by", columnDefinition = "CHAR(36)")
    private String grantedBy;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    // Audit fields
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "created_by", columnDefinition = "CHAR(36)")
    private String createdBy;
    
    @Column(name = "updated_by", columnDefinition = "CHAR(36)")
    private String updatedBy;
    
    @Column(name = "is_active")
    @Builder.Default
    private boolean active = true;
}
