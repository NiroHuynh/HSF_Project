package com.hsf_project.repository;

import com.hsf_project.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Tìm danh sách đặt vé của User, sắp xếp theo ngày đặt mới nhất
    // và bỏ qua các bản ghi đã bị xóa ẩn (isDeleted = false)
    List<Booking> findByUserIdAndIsDeletedFalseOrderByBookingDateDesc(Long userId);

    // Tra cứu booking theo mã (vnp_TxnRef từ VNPay trả về)
    Optional<Booking> findByBookingCodeAndIsDeletedFalse(String bookingCode);

    List<Booking> findByStatusAndExpiredAtBeforeAndIsDeletedFalse(String pending, LocalDateTime now);

    long countByUserIdAndIsDeletedFalseAndStatusAndNoteAndBookingDateAfter(Long userId, String status, String note, LocalDateTime bookingDate);

    @Query("SELECT b FROM Booking b WHERE b.status IN ('CONFIRMED', 'EXPORTED') AND (b.isDeleted IS NULL OR b.isDeleted = false) AND b.bookingDate >= :from AND b.bookingDate < :to")
    List<Booking> findPaidInRange(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
