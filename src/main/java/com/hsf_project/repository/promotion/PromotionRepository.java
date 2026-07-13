package com.hsf_project.repository.promotion;

import com.hsf_project.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    /**
     * Tìm các khuyến mãi đang kích hoạt, chưa xóa, và thời gian hiện tại nằm trong khoảng hợp lệ.
     */
    @Query("SELECT p FROM Promotion p WHERE p.status = 'ACTIVE' " +
            "AND (p.isDeleted = false OR p.isDeleted IS NULL) " +
            "AND :now BETWEEN p.startDate AND p.endDate")
    List<Promotion> findActivePromotions(@Param("now") LocalDateTime now);
    Optional<Promotion> findByCodeIgnoreCaseAndIsDeletedFalse(String code);
    List<Promotion> findByStatusAndIsDeletedFalse(String status);

    /** Tìm kiếm cho trang quản trị: chưa xoá, lọc theo code hoặc tên (kw null = tất cả). */
    @Query("SELECT p FROM Promotion p WHERE (p.isDeleted = false OR p.isDeleted IS NULL) " +
            "AND (:kw IS NULL OR LOWER(p.code) LIKE LOWER(CONCAT('%', :kw, '%')) " +
            "     OR LOWER(p.name) LIKE LOWER(CONCAT('%', :kw, '%'))) " +
            "ORDER BY p.id DESC")
    org.springframework.data.domain.Page<Promotion> searchAdmin(@Param("kw") String keyword,
                                                                org.springframework.data.domain.Pageable pageable);

    boolean existsByCodeIgnoreCaseAndIsDeletedFalse(String code);

    boolean existsByCodeIgnoreCaseAndIsDeletedFalseAndIdNot(String code, Long id);

    /**
     * Tăng lượt dùng bằng UPDATE nguyên tử (tránh race khi 2 giao dịch cùng chốt),
     * và không vượt quá usage_limit nếu có đặt giới hạn.
     */
    @Modifying
    @Query("UPDATE Promotion p SET p.usedCount = COALESCE(p.usedCount, 0) + 1 " +
            "WHERE p.id = :id AND (p.usageLimit IS NULL OR COALESCE(p.usedCount, 0) < p.usageLimit)")
    int incrementUsedCount(@Param("id") Long id);
}
