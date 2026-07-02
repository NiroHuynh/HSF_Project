package com.hsf_project.service.auth.impl;

import com.hsf_project.entity.User;
import com.hsf_project.repository.auth.UserRepository;
import com.hsf_project.service.auth.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepo;
    @Override
    public User loginByEmail(String email, String password) {
        return userRepo.findByEmailAndPasswordAndIsDeletedFalseAndStatus(email,password, "ACTIVE");
    }

}
