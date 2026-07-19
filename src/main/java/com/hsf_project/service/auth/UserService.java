package com.hsf_project.service.auth;

import com.hsf_project.entity.User;
import com.hsf_project.dto.RegisterForm;

import java.util.Optional;

public interface UserService {
    User loginByEmail(String email, String password);
    boolean emailExists(String email);
    User registerCustomer(RegisterForm form);
}
