package com.hsf_project.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface BookingConfirmService {

    /**
     * Dữ liệu cần để build URL VNPay sau khi đã chốt tiền phía server.
     *
     * @param bankCode kênh thanh toán VNPay suy ra từ phương thức user chọn (có thể null)
     */
    record ConfirmResult(String bookingCode, BigDecimal finalAmount, String bankCode,
                         LocalDateTime expiresAt) {

    }

    String confirmBooking(Long showtimeId, List<String> seatCodes, Map<Long, Integer> comboQuantities, Long paymentMethodId, Long promotionId, BigDecimal discountAmount);

    /**
     * Bước chọn ghế: tạo Booking/Ticket ở trạng thái PENDING, giữ ghế 15 phút.
     * Ném IllegalArgumentException nếu có ghế vừa bị người khác giữ.
     */
    ConfirmResult confirmBooking(Long userId, Long showtimeId, List<String> seatCodes, Map<Long, Integer> comboQuantities,
                                 Long paymentMethodId, Long promotionId,
                                 BigDecimal discountAmount);

    /**
     * Bước cuối trước khi sang VNPay: kiểm tra booking thuộc đúng user và còn hạn,
     * validate lại mã khuyến mãi PHÍA SERVER (không tin số tiền giảm từ client),
     * chốt finalAmount và tạo bản ghi Payment PENDING.
     */
    ConfirmResult preparePayment(String bookingCode, Long userId, Long paymentMethodId, String promoCode);

    /**
     * Chốt kết quả thanh toán từ VNPay Return URL.
     * Thành công: booking → CONFIRMED, ticket → PAID, payment → SUCCESS, đánh dấu voucher đã dùng.
     *   Nếu booking đã bị hủy vì quá hạn nhưng ghế còn trống thì khôi phục lại;
     *   ghế đã có người khác giữ thì ghi nhận cần hoàn tiền (payment → REFUND_REQUIRED).
     * Thất bại/hủy: booking → CANCELED, soft-delete ticket (giải phóng ghế), payment → FAILED.
     * Idempotent: gọi lại khi payment đã có paymentTime sẽ không thay đổi gì.
     */
    void finalizeBooking(String bookingCode, boolean success,
                         String transactionNo, String responseCode, String rawParams);
}
