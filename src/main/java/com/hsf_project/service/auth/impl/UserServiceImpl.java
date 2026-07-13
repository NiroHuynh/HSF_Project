package com.hsf_project.service.auth.impl;

import com.hsf_project.dto.auth.RegisterForm;
import com.hsf_project.entity.Role;
import com.hsf_project.entity.User;
import com.hsf_project.repository.auth.RoleRepository;
import com.hsf_project.repository.auth.UserRepository;
import com.hsf_project.service.auth.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private RoleRepository roleRepo;

    @Override
    public User loginByEmail(String email, String password) {
        return userRepo.findByEmailAndPasswordAndIsDeletedFalseAndStatus(email,password, "ACTIVE");
    }

    @Override
    @Transactional
    public User register(RegisterForm form) {
        if (userRepo.findByEmailAndIsDeletedFalse(form.getEmail()).isPresent()) {
            return null;
        }
        Role customerRole = roleRepo.findByRoleNameIgnoreCaseAndIsDeletedFalse("CUSTOMER")
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy role CUSTOMER trong DB"));

        User user = new User();
        user.setRole(customerRole);
        user.setFirstName(form.getFirstName());
        user.setLastName(form.getLastName());
        user.setEmail(form.getEmail());
        user.setPhoneNumber(form.getPhoneNumber() == null || form.getPhoneNumber().isBlank()
                ? null : form.getPhoneNumber());
        user.setPassword(form.getPassword());
        user.setStatus("ACTIVE");
        user.setIsDeleted(false);
        return userRepo.save(user);
    }

    @Override
    @Transactional
    public User changePassword(Long userId, String oldPassword, String newPassword) {
        Optional<User> found = userRepo.findById(userId);
        if (found.isEmpty() || !found.get().getPassword().equals(oldPassword)) {
            return null;
        }
        User user = found.get();
        user.setPassword(newPassword);
        return userRepo.save(user);
    }

    @Override
    @Transactional
    public boolean resetPassword(String email, String newPassword) {
        Optional<User> found = userRepo.findByEmailAndIsDeletedFalse(email);
        if (found.isEmpty()) {
            return false;
        }
        User user = found.get();
        user.setPassword(newPassword);
        userRepo.save(user);
        return true;
    }
}
