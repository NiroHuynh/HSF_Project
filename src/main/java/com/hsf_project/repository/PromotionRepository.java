package com.hsf_project.repository;

import com.hsf_project.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    Optional<Promotion> findByCodeIgnoreCaseAndIsDeletedFalse(String code);
}