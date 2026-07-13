package com.hsf_project.repository;

import com.hsf_project.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    Page<Notification> findByIsDeletedFalseOrderByCreatedAtDesc(Pageable pageable);
}
