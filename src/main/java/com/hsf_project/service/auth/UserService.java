package com.hsf_project.service.auth;

import com.hsf_project.entity.User;

import java.util.Optional;

public interface UserService {
    User loginByEmail(String email, String password);
}
