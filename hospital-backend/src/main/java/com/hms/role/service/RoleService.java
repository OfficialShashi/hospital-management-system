
package com.hms.role.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.hms.role.Role;
import com.hms.role.repository.RoleRepository;

@Service
public class RoleService {

    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    // Get all roles
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    // Get role by ID
    public Optional<Role> getRoleById(Long roleId) {
        return roleRepository.findById(roleId);
    }

    // Find role by name
    public Role getRoleByName(String roleName) {
        return roleRepository.findByRoleName(roleName);
    }

    // Check role existence
    public boolean roleExists(String roleName) {
        return roleRepository.existsByRoleName(roleName);
    }
}
