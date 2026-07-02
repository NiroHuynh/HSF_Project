package com.hsf_project.repository;

import com.hsf_project.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // Lấy bản ghi payment mới nhất của một booking (mỗi booking hiện chỉ có 1 payment)
    Optional<Payment> findFirstByBookingIdOrderByIdDesc(Long bookingId);
}
