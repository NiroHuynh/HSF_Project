package com.hsf_project.repository.auth;

import com.hsf_project.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User findByEmailAndPasswordAndIsDeletedFalseAndStatus(String email, String password, String status);

    Optional<User> findByEmailAndIsDeletedFalse(String email);

    //Lấy thời gian khóa mới nhất của User bằng ID
    @Query("SELECT u.lockBookingUntil FROM User u WHERE u.id = :userId")
    LocalDateTime getLockBookingUntilByUserId(@Param("userId") Long userId);
}
