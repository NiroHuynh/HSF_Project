package com.hsf_project.repository.admin;

import com.hsf_project.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AdminPromotionRepository extends JpaRepository<Promotion, Long> {
    @Query("select p from Promotion p where p.isDeleted = false or p.isDeleted is null order by p.id desc")
    List<Promotion> findAllVisible();
    Optional<Promotion> findByCodeIgnoreCase(String code);
}
