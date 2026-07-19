package com.hsf_project.repository.admin;

import com.hsf_project.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AdminRoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByRoleNameIgnoreCaseAndIsDeletedFalse(String roleName);
}
