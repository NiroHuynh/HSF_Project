package com.hsf_project.repository;

import com.hsf_project.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Tìm danh sách đặt vé của User, sắp xếp theo ngày đặt mới nhất
    // và bỏ qua các bản ghi đã bị xóa ẩn (isDeleted = false)
    List<Booking> findByUserIdAndIsDeletedFalseOrderByBookingDateDesc(Long userId);

    // Tra cứu booking theo mã (vnp_TxnRef từ VNPay trả về)
    Optional<Booking> findByBookingCodeAndIsDeletedFalse(String bookingCode);
}
