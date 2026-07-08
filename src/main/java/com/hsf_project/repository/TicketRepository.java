package com.hsf_project.repository;

import com.hsf_project.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
        AND t.status <> 'CANCELLED'
        AND t.isDeleted = false
        AND (t.booking.status = 'PAID' OR (t.booking.status = 'PENDING' AND t.booking.expiredAt > CURRENT_TIMESTAMP))
    """)
    boolean existsBookedSeat(
            @Param("seatId") Long seatId,
            @Param("showtimeId") Long showtimeId
    );
}
