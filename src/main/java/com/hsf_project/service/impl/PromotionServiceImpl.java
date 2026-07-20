package com.hsf_project.service.impl;

import com.hsf_project.dto.admin.PromotionForm;
import com.hsf_project.entity.Promotion;
import com.hsf_project.repository.PromotionRepository;
import com.hsf_project.service.PromotionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PromotionServiceImpl implements PromotionService {

    @Autowired
    private PromotionRepository promotionRepository;

    @Override
    public PromotionResult validate(String code, BigDecimal orderAmount) {
        if (code == null || code.isBlank()) {
            return new PromotionResult(false, "Vui lòng nhập mã.", BigDecimal.ZERO, null);
        }

        Promotion promo = promotionRepository.findByCodeIgnoreCaseAndIsDeletedFalse(code.trim()).orElse(null);

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
    @Transactional
    public void markUsed(Long promotionId) {
        promotionRepository.incrementUsedCount(promotionId);
    }

    /* ================= CRUD cho khu admin ================= */

    @Override
    public Page<Promotion> searchAdmin(String keyword, int page, int size) {
        String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        return promotionRepository.searchAdmin(kw, PageRequest.of(page, size));
    }

    @Override
    public Optional<Promotion> getById(Long id) {
        return promotionRepository.findById(id)
                .filter(p -> p.getIsDeleted() == null || !p.getIsDeleted());
    }

    @Override
    @Transactional
    public Promotion create(PromotionForm form) {
        Promotion promo = new Promotion();
        applyForm(promo, form);
        promo.setUsedCount(0);
        promo.setIsDeleted(false);
        return promotionRepository.save(promo);
    }

    @Override
    @Transactional
    public Promotion update(Long id, PromotionForm form) {
        Promotion promo = getById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy voucher #" + id));
        applyForm(promo, form);
        return promotionRepository.save(promo);
    }

    @Override
    @Transactional
    public void softDelete(Long id) {
        getById(id).ifPresent(promo -> {
            promo.setIsDeleted(true);
            promotionRepository.save(promo);
        });
    }

    @Override
    @Transactional
    public void toggleStatus(Long id) {
        getById(id).ifPresent(promo -> {
            promo.setStatus("ACTIVE".equals(promo.getStatus()) ? "INACTIVE" : "ACTIVE");
            promotionRepository.save(promo);
        });
    }

    private void applyForm(Promotion promo, PromotionForm form) {
        promo.setCode(form.getCode().trim().toUpperCase());
        promo.setName(form.getName().trim());
        promo.setDescription(form.getDescription() == null || form.getDescription().isBlank()
                ? null : form.getDescription().trim());
        promo.setDiscountType(form.getDiscountType());
        promo.setDiscountValue(form.getDiscountValue());
        promo.setStartDate(form.getStartDate());
        promo.setEndDate(form.getEndDate());
        promo.setUsageLimit(form.getUsageLimit());
        promo.setStatus(form.getStatus());
    }
}