package com.hsf_project.service;

import java.time.LocalDateTime;
import java.util.Map;

public interface ManagerDashboardService {

    /**
     * @param mode "today" | "month" | "quarter" | "year"
     *             Quyết định cách group dữ liệu biểu đồ:
     *             today   → 1 cột (ngày hôm nay)
     *             month   → 4 cột (4 tuần trong tháng)
     *             quarter → 3 cột (3 tháng trong quý)
     *             year    → 12 cột (12 tháng)
     */
    Map<String, Object> getStats(Integer cinemaId, LocalDateTime from, LocalDateTime to, String mode);

    Map<String, Object> searchBooking(String bookingCode, Integer cinemaId);
}