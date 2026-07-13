package com.hsf_project.service;

import com.hsf_project.dto.notification.response.NotificationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {

    Page<NotificationResponse> getNotifications(Pageable pageable);
}
