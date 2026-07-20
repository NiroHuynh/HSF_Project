package com.hsf_project.mapper;

import com.hsf_project.dto.customer.response.ActiveCustomerResponse;
import com.hsf_project.dto.customer.response.CustomerDetailResponse;
import com.hsf_project.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CustomerMapper {

    @Mapping(target = "customerId", source = "user.id")
    @Mapping(target = "fullName", expression = "java(user.getFullName())")
    @Mapping(target = "avatar", expression = "java(getAvatar(user))")
    @Mapping(target = "status", source = "user.status")
    @Mapping(target = "bookingCount", source = "bookingCount")
    @Mapping(target = "latestMovie", source = "latestMovie")
    ActiveCustomerResponse toActiveCustomerResponse(User user, int bookingCount, String latestMovie);

    @Mapping(target = "customerId", source = "user.id")
    @Mapping(target = "fullName", expression = "java(user.getFullName())")
    @Mapping(target = "phone", source = "user.phoneNumber")
    @Mapping(target = "birthday", source = "user.dateOfBirth")
    @Mapping(target = "gender", source = "user.gender")
    @Mapping(target = "status", source = "user.status")
    @Mapping(target = "totalBookings", source = "totalBookings")
    @Mapping(target = "totalSpent", source = "totalSpent")
    @Mapping(target = "memberLevel", source = "memberLevel")
    @Mapping(target = "latestBooking", source = "latestBooking")
    CustomerDetailResponse toCustomerDetailResponse(User user, int totalBookings, BigDecimal totalSpent, String memberLevel, LocalDateTime latestBooking);

    default String getAvatar(User user) {
        String name = user.getFullName();
        if (name != null && !name.isEmpty()) {
            return String.valueOf(name.charAt(0));
        }
        return "?";
    }
}
