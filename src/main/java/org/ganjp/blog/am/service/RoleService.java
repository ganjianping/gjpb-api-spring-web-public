package org.ganjp.blog.am.service;

import lombok.RequiredArgsConstructor;
import org.ganjp.blog.common.exception.ResourceNotFoundException;
import org.ganjp.blog.am.model.dto.request.RoleRequest;
import org.ganjp.blog.am.model.dto.request.RoleUpdateRequest;
import org.ganjp.blog.am.model.dto.response.RoleResponse;
import org.ganjp.blog.am.model.entity.Role;
import org.ganjp.blog.am.repository.RoleRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll(Sort.by(Sort.Direction.ASC, "displayOrder", "name"))
                .stream()
                .map(this::mapToRoleResponse)
                .collect(Collectors.toList());
    }

    public List<RoleResponse> getActiveRoles() {
        return roleRepository.findByActiveTrue()
                .stream()
                .map(this::mapToRoleResponse)
                .collect(Collectors.toList());
    }

    public RoleResponse getRoleById(String id) {
        return roleRepository.findById(id)
                .map(this::mapToRoleResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));
    }

    public RoleResponse getRoleByCode(String code) {
        return roleRepository.findByCode(code)
                .map(this::mapToRoleResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "code", code));
    }

    @Transactional
    public RoleResponse createRole(RoleRequest roleRequest, String username) {
        if (roleRepository.existsByCode(roleRequest.getCode())) {
            throw new RuntimeException("Role with code " + roleRequest.getCode() + " already exists");
        }

        Role role = new Role();
        role.setId(UUID.randomUUID().toString());
        role.setCode(roleRequest.getCode());
        role.setName(roleRequest.getName());
        role.setDescription(roleRequest.getDescription());
        role.setDisplayOrder(roleRequest.getDisplayOrder() != null ? roleRequest.getDisplayOrder() : 0);
        role.setActive(roleRequest.getActive() != null ? roleRequest.getActive() : true);
        role.setCreatedAt(LocalDateTime.now());
        role.setUpdatedAt(LocalDateTime.now());
        role.setCreatedBy(username);
        role.setUpdatedBy(username);

        Role savedRole = roleRepository.save(role);
        return mapToRoleResponse(savedRole);
    }

    @Transactional
    public RoleResponse updateRole(String id, RoleRequest roleRequest, String username) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));

        // Check if code is being changed and if new code already exists
        if (!role.getCode().equals(roleRequest.getCode()) && roleRepository.existsByCode(roleRequest.getCode())) {
            throw new RuntimeException("Role with code " + roleRequest.getCode() + " already exists");
        }

        role.setCode(roleRequest.getCode());
        role.setName(roleRequest.getName());
        role.setDescription(roleRequest.getDescription());
        role.setDisplayOrder(roleRequest.getDisplayOrder() != null ? roleRequest.getDisplayOrder() : role.getDisplayOrder());
        role.setActive(roleRequest.getActive() != null ? roleRequest.getActive() : role.isActive());
        role.setUpdatedAt(LocalDateTime.now());
        role.setUpdatedBy(username);

        Role updatedRole = roleRepository.save(role);
        return mapToRoleResponse(updatedRole);
    }

    @Transactional
    public RoleResponse updateRolePartially(String id, RoleUpdateRequest updateRequest, String username) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));

        // Check if code is being changed and if new code already exists
        if (updateRequest.getCode() != null && !updateRequest.getCode().equals(role.getCode()) 
                && roleRepository.existsByCode(updateRequest.getCode())) {
            throw new RuntimeException("Role with code " + updateRequest.getCode() + " already exists");
        }

        // Only update fields that are not null in the request
        if (updateRequest.getCode() != null) {
            role.setCode(updateRequest.getCode());
        }
        
        if (updateRequest.getName() != null) {
            role.setName(updateRequest.getName());
        }
        
        if (updateRequest.getDescription() != null) {
            role.setDescription(updateRequest.getDescription());
        }
        
        if (updateRequest.getDisplayOrder() != null) {
            role.setDisplayOrder(updateRequest.getDisplayOrder());
        }
        
        if (updateRequest.getActive() != null) {
            role.setActive(updateRequest.getActive());
        }
        
        role.setUpdatedAt(LocalDateTime.now());
        role.setUpdatedBy(username);

        Role updatedRole = roleRepository.save(role);
        return mapToRoleResponse(updatedRole);
    }

    @Transactional
    public void deleteRole(String id) {
        if (!roleRepository.existsById(id)) {
            throw new RuntimeException("Role not found with id: " + id);
        }
        roleRepository.deleteById(id);
    }

    @Transactional
    public RoleResponse toggleRoleStatus(String id, String username) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + id));
        
        role.setActive(!role.isActive());
        role.setUpdatedAt(LocalDateTime.now());
        role.setUpdatedBy(username);
        
        Role updatedRole = roleRepository.save(role);
        return mapToRoleResponse(updatedRole);
    }

    private RoleResponse mapToRoleResponse(Role role) {
        return RoleResponse.builder()
                .id(role.getId())
                .code(role.getCode())
                .name(role.getName())
                .description(role.getDescription())
                .displayOrder(role.getDisplayOrder())
                .active(role.isActive())
                .createdAt(role.getCreatedAt())
                .updatedAt(role.getUpdatedAt())
                .createdBy(role.getCreatedBy())
                .updatedBy(role.getUpdatedBy())
                .build();
    }
}