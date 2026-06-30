package com.hsf_project.repository;

import com.hsf_project.entity.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingPaymentRepository extends JpaRepository<PaymentMethod, Long> {
    List<PaymentMethod> findByIsActiveTrueOrderByIdAsc();
}