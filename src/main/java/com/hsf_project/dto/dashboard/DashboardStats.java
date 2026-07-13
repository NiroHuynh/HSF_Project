package com.hsf_project.dto.dashboard;

import java.math.BigDecimal;

/** Bộ số liệu tổng hợp doanh thu cho một khoảng thời gian. */
public record DashboardStats(
        BigDecimal totalRevenue,   // tổng thực thu (final_amount)
        long bookings,             // số đơn đặt vé đã thanh toán
        long ticketsSold,          // số vé đã bán
        BigDecimal ticketRevenue,  // doanh thu tiền vé (trước giảm giá)
        BigDecimal comboRevenue    // doanh thu bắp nước
) {
}
