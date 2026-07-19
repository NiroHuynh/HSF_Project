package com.hsf_project.service.auth.impl;

import com.hsf_project.entity.User;
import com.hsf_project.dto.RegisterForm;
import com.hsf_project.repository.RoleRepository;
import com.hsf_project.repository.auth.UserRepository;
import com.hsf_project.service.auth.UserService;
import jakarta.transaction.Transactional;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepo;
    @Autowired
    private RoleRepository roleRepository;

    @Override
    @Transactional
    public User loginByEmail(String email, String password) {
        User u = userRepo.findByEmailAndPasswordAndIsDeletedFalseAndStatus(email, password, "ACTIVE");

        if (u != null && u.getCinema() != null) {
            Hibernate.initialize(u.getCinema());
        }

        return u;
    }

    @Override
    public boolean emailExists(String email) {
        return email != null && userRepo.findByEmailAndIsDeletedFalse(email.trim()).isPresent();
    }

    @Override
    @Transactional
    public User registerCustomer(RegisterForm form) {
        String name = form.getFullName().trim().replaceAll("\\s+", " ");
        int split = name.lastIndexOf(' ');
        User user = new User();
        user.setLastName(split > 0 ? name.substring(0, split) : "");
        user.setFirstName(split > 0 ? name.substring(split + 1) : name);
        user.setEmail(form.getEmail().trim().toLowerCase());
        user.setPassword(form.getPassword());
        user.setRole(roleRepository.findByRoleNameIgnoreCaseAndIsDeletedFalse("CUSTOMER")
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy role CUSTOMER")));
        user.setStatus("ACTIVE");
        user.setDeleted(false);
        user.setCreatedAt(LocalDateTime.now());
        return userRepo.save(user);
    }

}
