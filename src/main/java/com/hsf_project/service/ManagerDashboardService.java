package com.hsf_project.service;

import java.time.LocalDateTime;
import java.util.Map;

public interface ManagerDashboardService {

    Map<String, Object> getStats(Integer cinemaId, LocalDateTime from, LocalDateTime to, String mode);

    Map<String, Object> searchBooking(String bookingCode, Integer cinemaId);

    /**
     * Đổi trạng thái booking từ CONFIRMED → EXPORTED.
     * Chỉ được phép xuất khi booking đã thanh toán (CONFIRMED).
     * @return Map với "success": true/false, "message" nếu thất bại
     */
    Map<String, Object> exportBooking(String bookingCode, Integer cinemaId);
}