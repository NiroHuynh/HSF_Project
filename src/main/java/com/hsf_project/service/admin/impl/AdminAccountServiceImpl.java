package com.hsf_project.service.admin.impl;

import com.hsf_project.dto.admin.response.AdminAccountResponse;
import com.hsf_project.entity.Role;
import com.hsf_project.entity.User;
import com.hsf_project.mapper.AdminAccountMapper;
import com.hsf_project.repository.auth.RoleRepository;
import com.hsf_project.repository.auth.UserRepository;
import com.hsf_project.service.admin.AdminAccountService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class AdminAccountServiceImpl implements AdminAccountService {

    UserRepository userRepository;
    RoleRepository roleRepository;
    AdminAccountMapper adminAccountMapper;

    private static final List<Integer> ADMIN_ROLE_IDS = List.of(1, 2);

    @Override
    public List<AdminAccountResponse> getAccounts(String keyword, String roleFilter) {
        List<User> users = userRepository.findByRole_IdInAndIsDeletedFalse(ADMIN_ROLE_IDS);

        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.toLowerCase();
            users = users.stream()
                    .filter(u -> u.getFullName().toLowerCase().contains(kw)
                            || u.getEmail().toLowerCase().contains(kw))
                    .toList();
        }

        if (roleFilter != null && !roleFilter.isBlank() && !"ALL".equalsIgnoreCase(roleFilter)) {
            users = users.stream()
                    .filter(u -> roleFilter.equalsIgnoreCase(u.getRole().getRoleName()))
                    .toList();
        }

        return adminAccountMapper.toResponseList(users);
    }

    @Override
    public long getTotalCount() {
        return userRepository.countByRole_IdInAndIsDeletedFalse(ADMIN_ROLE_IDS);
    }

    @Override
    public long getActiveCount() {
        return userRepository.countByRole_IdInAndStatusAndIsDeletedFalse(ADMIN_ROLE_IDS, "ACTIVE");
    }

    @Override
    public long getLockedCount() {
        return userRepository.countByRole_IdInAndStatusAndIsDeletedFalse(ADMIN_ROLE_IDS, "LOCKED");
    }

    @Override
    @Transactional
    public void toggleStatus(Long id, String newStatus) {
        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại"));
        user.setStatus(newStatus);
    }

    @Override
    @Transactional
    public void deleteAccount(Long id) {
        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại"));
        user.setIsDeleted(true);
    }

    @Override
    @Transactional
    public void createAccount(String email, String password, String firstName, String lastName,
                              String roleId, String phoneNumber) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email đã tồn tại trong hệ thống");
        }

        Role role = roleRepository.getReferenceById(Integer.parseInt(roleId));

        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRole(role);
        user.setPhoneNumber(phoneNumber);
        user.setStatus("ACTIVE");
        user.setIsDeleted(false);

        userRepository.save(user);
    }

    @Override
    public AdminAccountResponse getAccountById(Long id) {
        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại"));
        return adminAccountMapper.toResponse(user);
    }

    @Override
    @Transactional
    public void updateAccount(Long id, String email, String firstName, String lastName,
                              String phoneNumber, String roleId) {
        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại"));

        if (email != null && !email.isBlank()) {
            userRepository.findByEmail(email).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new RuntimeException("Email đã được sử dụng bởi tài khoản khác");
                }
            });
            user.setEmail(email);
        }
        if (firstName != null && !firstName.isBlank()) user.setFirstName(firstName);
        if (lastName != null && !lastName.isBlank()) user.setLastName(lastName);
        if (phoneNumber != null) user.setPhoneNumber(phoneNumber);
        if (roleId != null && !roleId.isBlank()) {
            user.setRole(roleRepository.getReferenceById(Integer.parseInt(roleId)));
        }
    }
}
