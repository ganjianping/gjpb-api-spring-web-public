package org.ganjp.blog.am.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "auth_roles")
public class Role {
    /**
     * Display order property for UI sorting (not stored in database)
     */
    @Transient
    @Builder.Default
    private Integer displayOrder = 0;
    @Id
    @Column(columnDefinition = "CHAR(36)")
    private String id;
    
    @Column(name = "code", length = 50, nullable = false)
    private String code;
    
    @Column(name = "name", length = 100, nullable = false)
    private String name;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private int sortOrder = 0;
    
    // Hierarchical role support
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_role_id", columnDefinition = "CHAR(36)")
    private Role parentRole;
    
    @OneToMany(mappedBy = "parentRole", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Role> childRoles = new ArrayList<>();
    
    @Column(name = "level")
    private int level = 0;
    
    @Column(name = "is_system_role")
    @Builder.Default
    private boolean systemRole = false;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "created_by", columnDefinition = "CHAR(36)")
    private String createdBy;
    
    @Column(name = "updated_by", columnDefinition = "CHAR(36)")
    private String updatedBy;
    
    @Column(name = "is_active")
    private boolean active;
}