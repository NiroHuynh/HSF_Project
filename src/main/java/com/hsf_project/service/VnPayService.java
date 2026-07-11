package com.hsf_project.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

public interface VnPayService {

    /**
     * Tạo URL thanh toán VNPay sandbox để redirect trình duyệt sang.
     *
     * @param bookingCode mã booking, dùng làm vnp_TxnRef (phải duy nhất mỗi giao dịch)
     * @param finalAmount số tiền thanh toán (VND)
     * @param bankCode    kênh thanh toán VNPay (VNBANK/INTCARD...), null để user chọn trên trang VNPay
     * @param clientIp    IP của khách (request.getRemoteAddr())
     * @param expireAt    hạn thanh toán — đồng bộ với hạn giữ ghế của booking, null = 15 phút
     */
    String buildPaymentUrl(String bookingCode, BigDecimal finalAmount, String bankCode, String clientIp,
                           LocalDateTime expireAt);

    /**
     * Kiểm tra chữ ký vnp_SecureHash của các tham số VNPay gửi về Return URL.
     */
    boolean verifyReturn(Map<String, String> allParams);
}
