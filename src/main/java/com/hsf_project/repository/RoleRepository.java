package com.hsf_project.repository;

import com.hsf_project.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByRoleNameIgnoreCaseAndIsDeletedFalse(String roleName);
}
