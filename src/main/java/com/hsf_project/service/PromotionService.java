package com.hsf_project.service;

import com.hsf_project.dto.admin.PromotionForm;
import com.hsf_project.entity.Promotion;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.Optional;

public interface PromotionService {

    PromotionResult validate(String code, BigDecimal orderAmount);

    /**
     * Kết quả kiểm tra mã khuyến mãi — không phải DTO "nhân bản" Entity,
     * mà là kết quả tính toán của 1 hành động validate (không có sẵn trong DB).
     */
    record PromotionResult(boolean valid, String message, BigDecimal discountAmount, Long promotionId) {
    }

    void markUsed(Long promotionId);

    /* ================= CRUD cho khu admin ================= */

    /** Tìm kiếm voucher chưa xoá theo code/tên, phân trang. */
    Page<Promotion> searchAdmin(String keyword, int page, int size);

    Optional<Promotion> getById(Long id);

    Promotion create(PromotionForm form);

    Promotion update(Long id, PromotionForm form);

    void softDelete(Long id);

    /** Đảo trạng thái ACTIVE <-> INACTIVE. */
    void toggleStatus(Long id);
}
