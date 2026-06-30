package com.se196222.mvc.choosecinema.repository;

import com.se196222.mvc.choosecinema.entity.ShowTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ShowTimeRepository extends JpaRepository<ShowTime, Integer> {
    // ShowTimes for a specific cinema on a specific date
    // Join: showtime → room → cinema
    @Query("SELECT st FROM ShowTime st " +
            "JOIN FETCH st.room r " +
            "JOIN FETCH st.movie m " +
            "WHERE r.cinema.id = :cinemaId " +
            "  AND CAST(st.startTime AS LocalDate) = :date " +
            "  AND st.isDeleted = false " +
            "  AND r.isDeleted = false " +
            "  AND m.isDeleted = false " +
            "ORDER BY m.title, r.roomType, st.startTime")
    List<ShowTime> findByCinemaIdAndDate(@Param("cinemaId") Integer cinemaId,
                                         @Param("date") LocalDate date);
}
