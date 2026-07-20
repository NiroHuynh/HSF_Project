package com.hsf_project.service.impl;

import com.hsf_project.dto.notification.response.NotificationResponse;
import com.hsf_project.entity.Notification;
import com.hsf_project.mapper.NotificationMapper;
import com.hsf_project.repository.NotificationRepository;
import com.hsf_project.service.NotificationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class NotificationServiceImpl implements NotificationService {

    NotificationRepository notificationRepository;
    NotificationMapper notificationMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getNotifications(Pageable pageable) {
        Page<Notification> notifications = notificationRepository.findByIsDeletedFalseOrderByCreatedAtDesc(pageable);
        return notifications.map(notificationMapper::toNotificationResponse);
    }
}
