package com.hsf_project.repository;

import com.hsf_project.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatRepository
        extends JpaRepository<Seat, Long> {


    @Query("""
    SELECT s
    FROM Seat s
    WHERE s.room.id = :roomId
    AND s.isDeleted = false
    AND s.isActive = true
    ORDER BY s.rowLabel, s.seatNumber
    """)
    List<Seat> findSeatsByRoom(
            Integer roomId
    );


    

}
