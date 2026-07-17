package com.hsf_project.service;

import java.math.BigDecimal;

public interface PromotionService {

    PromotionResult validate(String code, BigDecimal orderAmount);

    /**
     * Kết quả kiểm tra mã khuyến mãi — không phải DTO "nhân bản" Entity,
     * mà là kết quả tính toán của 1 hành động validate (không có sẵn trong DB).
     */
    record PromotionResult(boolean valid, String message, BigDecimal discountAmount, Long promotionId) {
    }

    void markUsed(Long promotionId);
}
