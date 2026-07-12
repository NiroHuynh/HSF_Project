package com.hsf_project.dto.customer.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import com.hsf_project.entity.User;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomerDetailResponse {
    Long customerId;
    String fullName;
    String email;
    String phone;
    LocalDate birthday;
    String gender;
    String status;
    int totalBookings;
    BigDecimal totalSpent;
    String memberLevel;
    LocalDateTime latestBooking;
}
