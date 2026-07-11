package com.hsf_project.repository;

import com.hsf_project.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket,Long> {

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
    /**
     * Đếm số ghế đã đặt theo từng suất chiếu — batch query tránh N+1.
     * Trả về Object[]: [0]=showtimeId, [1]=bookedCount
     */
    @Query("SELECT t.showtime.id, COUNT(t) FROM Ticket t " +
            "JOIN t.booking b " +
            "WHERE t.showtime.id IN :showtimeIds " +
            "AND b.isDeleted = false AND b.status != 'CANCELED' " +
            "GROUP BY t.showtime.id")
    List<Object[]> countBookedByShowtimeIds(@Param("showtimeIds") List<Long> showtimeIds);

    /**
     * Đếm số vé còn hiệu lực của 1 suất chiếu — dùng để kiểm tra trước khi xóa.
     * Nếu kết quả > 0 → không cho xóa vì đã có khách đặt.
     */
    @Query("SELECT COUNT(t) FROM Ticket t JOIN t.booking b " +
            "WHERE t.showtime.id = :showtimeId " +
            "AND b.isDeleted = false AND b.status != 'CANCELED'")
    Long countActiveTicketsByShowtimeId(@Param("showtimeId") Long showtimeId);
}
