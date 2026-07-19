package com.hsf_project.dto.admin.response;

import java.math.BigDecimal;

/** Một khách hàng trong danh sách khi admin lọc theo một chi nhánh cụ thể. */
public record CinemaCustomerRow(
        Long userId,
        String fullName,
        String email,
        String phoneNumber,
        long bookingCount,
        BigDecimal totalSpent) {
}
