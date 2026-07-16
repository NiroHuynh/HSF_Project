package com.hsf_project.repository;

import com.hsf_project.entity.CinemaRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CinemaRoomRepository extends JpaRepository<CinemaRoom, Integer> {
    List<CinemaRoom> findByIsDeletedFalseOrderByCinemaNameAscNameAsc();
    List<CinemaRoom> findByRoomTypeAndIsDeletedFalse(String roomType);
    long countByCinemaIdAndIsDeletedFalse(Integer cinemaId);
}
