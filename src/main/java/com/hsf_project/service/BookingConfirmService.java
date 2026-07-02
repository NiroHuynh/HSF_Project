package com.hsf_project.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface    BookingConfirmService {
    String confirmBooking(Long showtimeId, List<String> seatCodes, Map<Long, Integer> comboQuantities,
                          Long paymentMethodId, Long promotionId,
                          BigDecimal discountAmount);

    /**
     * Kết quả tạo booking chờ thanh toán: dữ liệu cần để build URL VNPay.
     *
     * @param bankCode kênh thanh toán VNPay suy ra từ phương thức user chọn (có thể null)
     */
    record ConfirmResult(String bookingCode, BigDecimal finalAmount, String bankCode) {}

    /**
     * Tạo Booking/Ticket/Payment ở trạng thái PENDING, chờ kết quả từ VNPay.
     */
    ConfirmResult confirmBooking(Long userId, Long showtimeId, List<String> seatCodes, Map<Long, Integer> comboQuantities,
                                 Long paymentMethodId, Long promotionId,
                                 BigDecimal discountAmount);

    /**
     * Chốt kết quả thanh toán từ VNPay Return URL.
     * Thành công: booking/ticket → PAID, payment → SUCCESS, đánh dấu voucher đã dùng.
     * Thất bại/hủy: booking/ticket → CANCELLED (giải phóng ghế), payment → FAILED.
     * Idempotent: gọi lại với booking không còn PENDING sẽ không thay đổi gì.
     */
    void finalizeBooking(String bookingCode, boolean success,
                         String transactionNo, String responseCode, String rawParams);
}
