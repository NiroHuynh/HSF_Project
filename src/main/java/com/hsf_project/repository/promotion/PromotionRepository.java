package com.hsf_project.repository.promotion;

import com.hsf_project.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
