package com.hsf_project.repository.auth;

import com.hsf_project.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User findByEmailAndPasswordAndIsDeletedFalseAndStatus(String email, String password, String status);

    Optional<User> findByEmailAndIsDeletedFalse(String email);
}
