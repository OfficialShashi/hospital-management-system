
package com.hms.role.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hms.role.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    // Find role using role name
    Role findByRoleName(String roleName);

    // Check whether role exists
    boolean existsByRoleName(String roleName);
}
