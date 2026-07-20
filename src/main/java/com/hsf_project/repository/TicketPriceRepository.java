package com.hsf_project.repository;

import com.hsf_project.entity.TicketPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketPriceRepository extends JpaRepository<TicketPrice, Long> {
    Optional<TicketPrice> findByRoomIdAndSeatTypeAndIsDeletedFalse(Integer roomId, String seatType);
    List<TicketPrice> findByRoomRoomTypeAndSeatTypeAndIsDeletedFalse(String roomType, String seatType);
    List<TicketPrice> findByRoomIdAndIsDeletedFalse(Integer roomId);
}
