package com.hsf_project.repository;

import com.hsf_project.entity.ShowTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ShowTimeRepository extends JpaRepository<ShowTime, Long> {

    @Query("SELECT st FROM ShowTime st " +
            "JOIN FETCH st.room r " +
            "JOIN FETCH r.cinema c " +
            "JOIN FETCH st.movie m " +
            "WHERE st.id = :id")
    Optional<ShowTime> findDetailById(Long id);

    // Lọc theo ngày bằng khoảng [đầu ngày, đầu ngày hôm sau) thay vì FUNCTION('DATE', ...)
    // vì hàm DATE() không tồn tại trên SQL Server.
    @Query("SELECT st FROM ShowTime st " +
            "JOIN FETCH st.room r " +
            "JOIN FETCH st.movie m " +
            "WHERE r.cinema.id = :cinemaId " +
            "AND st.startTime >= :startOfDay AND st.startTime < :endOfDay " +
            "AND st.isDeleted = false " +
            "AND r.isDeleted = false " +
            "ORDER BY m.title, r.roomType, st.startTime")
    List<ShowTime> findByCinemaAndDate(
            @Param("cinemaId") Integer cinemaId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay);
}