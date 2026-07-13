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

    @Query("SELECT b FROM Booking b WHERE b.status = 'PAID' AND (b.isDeleted IS NULL OR b.isDeleted = false) AND b.bookingDate >= :from AND b.bookingDate < :to")
    List<Booking> findPaidInRange(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    /* ================== Aggregate cho dashboard doanh thu ================== */

    /** Tổng doanh thu thực thu (final_amount) của các booking đã thanh toán trong [from, to). */
    @Query("SELECT COALESCE(SUM(b.finalAmount), 0) FROM Booking b " +
            "WHERE b.status = 'PAID' AND (b.isDeleted IS NULL OR b.isDeleted = false) " +
            "AND b.bookingDate >= :from AND b.bookingDate < :to")
    java.math.BigDecimal sumRevenue(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    /** Số booking đã thanh toán trong [from, to). */
    @Query("SELECT COUNT(b) FROM Booking b " +
            "WHERE b.status = 'PAID' AND (b.isDeleted IS NULL OR b.isDeleted = false) " +
            "AND b.bookingDate >= :from AND b.bookingDate < :to")
    long countPaid(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    /** Doanh thu theo từng tháng của một năm: mỗi phần tử = [tháng (1-12), tổng doanh thu]. */
    @Query("SELECT FUNCTION('MONTH', b.bookingDate), COALESCE(SUM(b.finalAmount), 0) FROM Booking b " +
            "WHERE b.status = 'PAID' AND (b.isDeleted IS NULL OR b.isDeleted = false) " +
            "AND FUNCTION('YEAR', b.bookingDate) = :year " +
            "GROUP BY FUNCTION('MONTH', b.bookingDate)")
    List<Object[]> monthlyRevenue(@Param("year") int year);

    /**
     * Booking đã thanh toán kèm rạp (mọi vé trong 1 booking cùng suất chiếu nên cùng rạp).
     * DISTINCT để mỗi booking chỉ ra 1 dòng dù có nhiều vé — tổng hợp SUM làm ở tầng service
     * để tránh doanh thu bị nhân bản theo số vé.
     * Mỗi phần tử = [cinemaId, cinemaName, bookingId, finalAmount].
     */
    @Query("SELECT DISTINCT c.id, c.name, b.id, b.finalAmount FROM Booking b " +
            "JOIN b.tickets t JOIN t.showtime st JOIN st.room r JOIN r.cinema c " +
            "WHERE b.status = 'PAID' AND (b.isDeleted IS NULL OR b.isDeleted = false) " +
            "AND b.bookingDate >= :from AND b.bookingDate < :to")
    List<Object[]> paidBookingsWithCinema(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    /* ---------- Bản giới hạn theo rạp (trang Manager) ---------- */

    @Query("SELECT COALESCE(SUM(b.finalAmount), 0) FROM Booking b " +
            "WHERE b.status = 'PAID' AND (b.isDeleted IS NULL OR b.isDeleted = false) " +
            "AND b.bookingDate >= :from AND b.bookingDate < :to " +
            "AND EXISTS (SELECT 1 FROM Ticket t WHERE t.booking = b AND t.showtime.room.cinema.id = :cinemaId)")
    java.math.BigDecimal sumRevenueByCinema(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to,
                                            @Param("cinemaId") Integer cinemaId);

    @Query("SELECT COUNT(b) FROM Booking b " +
            "WHERE b.status = 'PAID' AND (b.isDeleted IS NULL OR b.isDeleted = false) " +
            "AND b.bookingDate >= :from AND b.bookingDate < :to " +
            "AND EXISTS (SELECT 1 FROM Ticket t WHERE t.booking = b AND t.showtime.room.cinema.id = :cinemaId)")
    long countPaidByCinema(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to,
                           @Param("cinemaId") Integer cinemaId);

    @Query("SELECT FUNCTION('MONTH', b.bookingDate), COALESCE(SUM(b.finalAmount), 0) FROM Booking b " +
            "WHERE b.status = 'PAID' AND (b.isDeleted IS NULL OR b.isDeleted = false) " +
            "AND FUNCTION('YEAR', b.bookingDate) = :year " +
            "AND EXISTS (SELECT 1 FROM Ticket t WHERE t.booking = b AND t.showtime.room.cinema.id = :cinemaId) " +
            "GROUP BY FUNCTION('MONTH', b.bookingDate)")
    List<Object[]> monthlyRevenueByCinema(@Param("year") int year, @Param("cinemaId") Integer cinemaId);
}
