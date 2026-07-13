package com.hsf_project.repository;

import com.hsf_project.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketRepository extends JpaRepository<Ticket,Long> {
    long countByTicketPriceRoomId(Integer roomId);

    @Query("""
        SELECT CASE
            WHEN COUNT(t) > 0 THEN true
            ELSE false
        END
        FROM Ticket t
        WHERE t.seat.id = :seatId
        AND t.showtime.id = :showtimeId
        AND t.isDeleted = false
        AND (t.booking.status = 'PAID' OR (t.booking.status = 'PENDING' AND t.booking.expiredAt > CURRENT_TIMESTAMP))
    """)
    boolean existsBookedSeat(
            @Param("seatId") Long seatId,
            @Param("showtimeId") Long showtimeId
    );

    /**
     * Như existsBookedSeat nhưng bỏ qua vé của chính booking đang xét —
     * dùng khi chốt kết quả VNPay để biết ghế đã bị booking KHÁC giữ hay chưa.
     */
    @Query("""
        SELECT CASE
            WHEN COUNT(t) > 0 THEN true
            ELSE false
        END
        FROM Ticket t
        WHERE t.seat.id = :seatId
        AND t.showtime.id = :showtimeId
        AND t.booking.id <> :excludeBookingId
        AND t.isDeleted = false
        AND (t.booking.status = 'PAID' OR (t.booking.status = 'PENDING' AND t.booking.expiredAt > CURRENT_TIMESTAMP))
    """)
    boolean existsBookedSeatForOtherBooking(
            @Param("seatId") Long seatId,
            @Param("showtimeId") Long showtimeId,
            @Param("excludeBookingId") Long excludeBookingId
    );

    /* ================== Aggregate cho dashboard doanh thu ================== */

    /** Số vé đã bán (booking PAID) trong [from, to). */
    @Query("SELECT COUNT(t) FROM Ticket t JOIN t.booking b " +
            "WHERE b.status = 'PAID' AND (b.isDeleted IS NULL OR b.isDeleted = false) " +
            "AND (t.isDeleted IS NULL OR t.isDeleted = false) " +
            "AND b.bookingDate >= :from AND b.bookingDate < :to")
    long countPaidTickets(@Param("from") java.time.LocalDateTime from, @Param("to") java.time.LocalDateTime to);

    /** Doanh thu tiền vé (chưa trừ giảm giá) trong [from, to). */
    @Query("SELECT COALESCE(SUM(t.ticketPrice.price), 0) FROM Ticket t JOIN t.booking b " +
            "WHERE b.status = 'PAID' AND (b.isDeleted IS NULL OR b.isDeleted = false) " +
            "AND (t.isDeleted IS NULL OR t.isDeleted = false) " +
            "AND b.bookingDate >= :from AND b.bookingDate < :to")
    java.math.BigDecimal ticketRevenue(@Param("from") java.time.LocalDateTime from, @Param("to") java.time.LocalDateTime to);

    /* ---------- Bản giới hạn theo rạp (trang Manager) ---------- */

    @Query("SELECT COUNT(t) FROM Ticket t JOIN t.booking b " +
            "WHERE b.status = 'PAID' AND (b.isDeleted IS NULL OR b.isDeleted = false) " +
            "AND (t.isDeleted IS NULL OR t.isDeleted = false) " +
            "AND b.bookingDate >= :from AND b.bookingDate < :to " +
            "AND t.showtime.room.cinema.id = :cinemaId")
    long countPaidTicketsByCinema(@Param("from") java.time.LocalDateTime from, @Param("to") java.time.LocalDateTime to,
                                  @Param("cinemaId") Integer cinemaId);

    @Query("SELECT COALESCE(SUM(t.ticketPrice.price), 0) FROM Ticket t JOIN t.booking b " +
            "WHERE b.status = 'PAID' AND (b.isDeleted IS NULL OR b.isDeleted = false) " +
            "AND (t.isDeleted IS NULL OR t.isDeleted = false) " +
            "AND b.bookingDate >= :from AND b.bookingDate < :to " +
            "AND t.showtime.room.cinema.id = :cinemaId")
    java.math.BigDecimal ticketRevenueByCinema(@Param("from") java.time.LocalDateTime from, @Param("to") java.time.LocalDateTime to,
                                               @Param("cinemaId") Integer cinemaId);
}
