package com.hsf_project.dto.dashboard;

import java.math.BigDecimal;

/** Doanh thu tổng hợp theo từng chi nhánh rạp. */
public record CinemaRevenueDto(Integer cinemaId, String cinemaName, long bookings, BigDecimal revenue) {
}
