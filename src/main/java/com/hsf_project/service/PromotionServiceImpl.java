package com.hsf_project.service;

import com.hsf_project.entity.Promotion;
import com.hsf_project.repository.PromotionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class PromotionServiceImpl implements PromotionService {

    @Autowired
    private PromotionRepository promotionRepository;

    @Override
    public PromotionResult validate(String code, BigDecimal orderAmount) {
        if (code == null || code.isBlank()) {
            return new PromotionResult(false, "Vui lòng nhập mã.", BigDecimal.ZERO, null);
        }

        Promotion promo = promotionRepository.findByCodeIgnoreCaseAndIsDeletedFalse(code.trim())
                .orElse(null);

        if (promo == null) {
            return new PromotionResult(false, "Mã không tồn tại.", BigDecimal.ZERO, null);
        }
        if (!"ACTIVE".equals(promo.getStatus())) {
            return new PromotionResult(false, "Mã đã bị khoá.", BigDecimal.ZERO, null);
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(promo.getStartDate()) || now.isAfter(promo.getEndDate())) {
            return new PromotionResult(false, "Mã đã hết hạn hoặc chưa bắt đầu.", BigDecimal.ZERO, null);
        }
        if (promo.getUsageLimit() != null && promo.getUsedCount() >= promo.getUsageLimit()) {
            return new PromotionResult(false, "Mã đã hết lượt sử dụng.", BigDecimal.ZERO, null);
        }

        BigDecimal discount = "PERCENT".equals(promo.getDiscountType())
                ? orderAmount.multiply(promo.getDiscountValue()).divide(new BigDecimal("100"))
                : promo.getDiscountValue();

        if (discount.compareTo(orderAmount) > 0) {
            discount = orderAmount; // không giảm vượt quá tổng tiền đơn hàng
        }

        return new PromotionResult(true, "Áp dụng mã thành công.", discount, promo.getId());
    }

    @Override
    public void markUsed(Long promotionId) {

    }
}