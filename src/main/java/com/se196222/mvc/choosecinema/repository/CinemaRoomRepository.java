package com.se196222.mvc.choosecinema.repository;

import com.se196222.mvc.choosecinema.entity.CinemaRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CinemaRoomRepository extends JpaRepository<CinemaRoom, Integer> {
    // Rooms of a cinema (for deriving supported formats)
    List<CinemaRoom> findByCinemaCinemaIdAndIsDeletedFalse(Integer cinemaId);

    // Distinct room_type values for a cinema
    @Query("SELECT DISTINCT r.roomType FROM CinemaRoom r " +
            "WHERE r.cinema.id = :cinemaId AND r.isDeleted = false")
    List<String> findDistinctRoomTypesByCinemaId(@Param("cinemaId") Integer cinemaId);
}
