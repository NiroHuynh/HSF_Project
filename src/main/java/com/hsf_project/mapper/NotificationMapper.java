package com.hsf_project.mapper;

import com.hsf_project.dto.notification.response.NotificationResponse;
import com.hsf_project.entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NotificationMapper {

    @Mapping(target = "notificationId", source = "id")
    @Mapping(target = "isRead", expression = "java(false)")
    NotificationResponse toNotificationResponse(Notification notification);

    List<NotificationResponse> toNotificationResponseList(List<Notification> notifications);
}
