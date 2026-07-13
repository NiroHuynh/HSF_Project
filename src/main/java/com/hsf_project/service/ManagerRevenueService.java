package com.hsf_project.service;

import java.time.LocalDateTime;
import java.util.Map;

public interface ManagerRevenueService {

    /**
     * Thống kê doanh thu chi tiết theo cinema + date range + mode.
     * mode: "today" | "month" | "quarter" | "year"
     *
     * Trả về:
     *   totalRevenue, ticketRevenue, comboRevenue (String formatted)
     *   ticketCount, comboCount (Long)
     *   ticketPercent, comboPercent (String)
     *   chartLabels (List<String>), ticketChartData, comboChartData (long[])
     *   topMovies (List<Map>) — title, tickets, revenue, barWidth (0-100)
     */
    Map<String, Object> getRevenueStats(Integer cinemaId, LocalDateTime from, LocalDateTime to, String mode);
}