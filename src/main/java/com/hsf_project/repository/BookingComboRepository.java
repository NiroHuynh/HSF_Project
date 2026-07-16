package com.hsf_project.repository;

import com.hsf_project.entity.BookingCombo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingComboRepository extends JpaRepository<BookingCombo, Long> {

    /** Xóa toàn bộ combo cũ của một booking — dùng khi user quay lại sửa lựa chọn combo. */
    @Modifying
    @Query("DELETE FROM BookingCombo bc WHERE bc.booking.id = :bookingId")
    void deleteByBookingId(@Param("bookingId") Long bookingId);

    /** Doanh thu bắp nước (combo) của các booking đã thanh toán trong [from, to). */
    @Query("SELECT COALESCE(SUM(bc.totalPrice), 0) FROM BookingCombo bc JOIN bc.booking b " +
            "WHERE b.status IN ('CONFIRMED', 'EXPORTED') AND (b.isDeleted IS NULL OR b.isDeleted = false) " +
            "AND b.bookingDate >= :from AND b.bookingDate < :to")
    java.math.BigDecimal comboRevenue(@Param("from") java.time.LocalDateTime from,
                                      @Param("to") java.time.LocalDateTime to);

    /** Doanh thu bắp nước giới hạn theo rạp (trang Manager). */
    @Query("SELECT COALESCE(SUM(bc.totalPrice), 0) FROM BookingCombo bc JOIN bc.booking b " +
            "WHERE b.status IN ('CONFIRMED', 'EXPORTED') AND (b.isDeleted IS NULL OR b.isDeleted = false) " +
            "AND b.bookingDate >= :from AND b.bookingDate < :to " +
            "AND EXISTS (SELECT 1 FROM Ticket t WHERE t.booking = b AND t.showtime.room.cinema.id = :cinemaId)")
    java.math.BigDecimal comboRevenueByCinema(@Param("from") java.time.LocalDateTime from,
                                              @Param("to") java.time.LocalDateTime to,
                                              @Param("cinemaId") Integer cinemaId);
}
