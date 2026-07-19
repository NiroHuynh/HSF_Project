package com.hsf_project.dto.admin.response;

/** Một dòng trong bảng "khách hàng theo chi nhánh" của admin. */
public record CinemaCustomerStats(
        Integer cinemaId,
        String cinemaName,
        String cityName,
        long customerCount,
        long bookingCount) {
}
