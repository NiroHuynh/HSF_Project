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
}
