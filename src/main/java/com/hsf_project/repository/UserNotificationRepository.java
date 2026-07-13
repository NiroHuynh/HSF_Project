package com.hsf_project.repository;

import com.hsf_project.entity.UserNotification;
import com.hsf_project.entity.UserNotificationId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserNotificationRepository extends JpaRepository<UserNotification, UserNotificationId> {
}
