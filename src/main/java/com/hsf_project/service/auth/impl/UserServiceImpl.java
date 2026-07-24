package com.hsf_project.service.auth.impl;

import com.hsf_project.dto.auth.RegisterForm;
import com.hsf_project.entity.Role;
import com.hsf_project.entity.User;
import com.hsf_project.repository.auth.RoleRepository;
import com.hsf_project.repository.auth.UserRepository;
import com.hsf_project.service.auth.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private RoleRepository roleRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public User loginByEmail(String email, String password) {
        // 1. Tìm user theo email (và còn hoạt động)
        Optional<User> userOpt = userRepo.findByEmailAndIsDeletedFalse(email);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // 2. Kiểm tra status và so sánh mật khẩu raw với mật khẩu đã hash trong DB
            if ("ACTIVE".equalsIgnoreCase(user.getStatus())
                    && passwordEncoder.matches(password, user.getPassword())) {
                return user;
            }
        }
        return null;
    }

    @Override
    public boolean isPhoneTaken(String phoneNumber) {
        return phoneNumber != null && !phoneNumber.isBlank()
                && userRepo.existsByPhoneNumberAndIsDeletedFalse(phoneNumber.trim());
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
        user.setPassword(passwordEncoder.encode(form.getPassword()));
        user.setStatus("ACTIVE");
        user.setIsDeleted(false);
        return userRepo.save(user);
    }

    @Override
    @Transactional
    public User changePassword(Long userId, String oldPassword, String newPassword) {
        Optional<User> found = userRepo.findById(userId);
        if (found.isEmpty() || !passwordEncoder.matches(oldPassword, found.get().getPassword())) {
            return null;
        }
        User user = found.get();
        user.setPassword(passwordEncoder.encode(newPassword));
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
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepo.save(user);
        return true;
    }

    @Override
    public long getRemainingLockMinutes(Long userId) {
        // 1. Truy vấn mốc thời gian bị khóa dưới DB
        LocalDateTime lockUntil = userRepo.getLockBookingUntilByUserId(userId);

        if (lockUntil == null) {
            return 0; // Không bị khóa
        }

        // 2. Lấy thời gian hiện tại chuẩn Việt Nam
        LocalDateTime currentLocalTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")).toLocalDateTime();

        // 3. Nếu giờ hiện tại vẫn chưa qua mốc bị khóa -> Đang trong thời gian phạt
        if (currentLocalTime.isBefore(lockUntil)) {
            long minutesLeft = Duration.between(currentLocalTime, lockUntil).toMinutes();
            return minutesLeft <= 0 ? 1 : minutesLeft; // Tối thiểu tính là 1 phút
        }

        return 0; // Đã hết hạn khóa
    }
}
