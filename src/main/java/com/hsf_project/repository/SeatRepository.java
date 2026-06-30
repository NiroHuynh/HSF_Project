package com.hsf_project.repository;

import com.hsf_project.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    Optional<Seat> findByRoomIdAndSeatCodeAndIsDeletedFalse(Integer roomId, String seatCode);
}
