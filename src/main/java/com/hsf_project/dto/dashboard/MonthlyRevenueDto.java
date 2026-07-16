package com.hsf_project.dto.dashboard;

import java.math.BigDecimal;

/** Doanh thu của một tháng trong năm (dùng cho biểu đồ cột). */
public record MonthlyRevenueDto(int month, BigDecimal revenue) {
}
