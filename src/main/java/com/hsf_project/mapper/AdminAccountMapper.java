package com.hsf_project.mapper;

import com.hsf_project.dto.admin.response.AdminAccountResponse;
import com.hsf_project.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AdminAccountMapper {

    @Mapping(target = "fullName", expression = "java(user.getFullName())")
    @Mapping(target = "role", source = "user.role.roleName")
    @Mapping(target = "createdDate", expression = "java(formatDate(user.getCreatedAt()))")
    AdminAccountResponse toResponse(User user);

    List<AdminAccountResponse> toResponseList(List<User> users);

    default String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
}
