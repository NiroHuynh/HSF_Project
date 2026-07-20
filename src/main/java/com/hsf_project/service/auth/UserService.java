package com.hsf_project.service.auth;

import com.hsf_project.dto.auth.RegisterForm;
import com.hsf_project.entity.User;

public interface UserService {

    User loginByEmail(String email, String password);

    /**
     * Đăng ký tài khoản mới với role CUSTOMER.
     * @return user vừa tạo, hoặc null nếu email đã tồn tại.
     */
    User register(RegisterForm form);

    /** Số điện thoại đã thuộc về một tài khoản chưa bị xóa hay chưa. */
    boolean isPhoneTaken(String phoneNumber);

    /**
     * Đổi mật khẩu: kiểm tra mật khẩu cũ trước khi cập nhật.
     * @return user sau khi đổi, hoặc null nếu mật khẩu cũ sai / user không tồn tại.
     */
    User changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * Quên mật khẩu: đặt lại mật khẩu mới theo email (không cần xác nhận email).
     * @return true nếu email tồn tại và đã cập nhật.
     */
    boolean resetPassword(String email, String newPassword);
}
