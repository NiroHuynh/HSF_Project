package com.hsf_project.repository;

import com.hsf_project.entity.ShowTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ShowTimeRepository extends JpaRepository<ShowTime, Long> {

    List<ShowTime> findByMovieIdAndIsDeletedFalse(Integer movieId);

    @Query("SELECT COUNT(st) FROM ShowTime st WHERE st.movie.id = :movieId AND (st.isDeleted IS NULL OR st.isDeleted = false) AND st.startTime > :now")
    long countUpcomingByMovie(@Param("movieId") Integer movieId, @Param("now") LocalDateTime now);

    @Query("SELECT DISTINCT b.user.id FROM Booking b JOIN b.tickets t WHERE t.showtime.movie.id = :movieId AND b.isDeleted = false AND t.isDeleted = false")
    List<Long> findDistinctUserIdsByMovieId(@Param("movieId") Integer movieId);

    @Query("SELECT st FROM ShowTime st " +
            "JOIN FETCH st.room r JOIN FETCH r.cinema c JOIN FETCH st.movie m " +
            "WHERE st.id = :id")
    Optional<ShowTime> findDetailById(Long id);

    @Query("SELECT st FROM ShowTime st " +
            "JOIN FETCH st.room r JOIN FETCH st.movie m " +
            "WHERE r.cinema.id = :cinemaId " +
            "AND st.startTime >= :startOfDay AND st.startTime < :endOfDay " +
            "AND (st.isDeleted IS NULL OR st.isDeleted = false) " +
            "AND (r.isDeleted IS NULL OR r.isDeleted = false) " +
            "ORDER BY m.title, r.roomType, st.startTime")
    List<ShowTime> findByCinemaAndDate(
            @Param("cinemaId") Integer cinemaId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay);

    @Query("SELECT COUNT(st) FROM ShowTime st " +
            "WHERE st.room.cinema.id = :cinemaId " +
            "AND (st.isDeleted IS NULL OR st.isDeleted = false) " +
            "AND st.startTime BETWEEN :from AND :to")
    Long countByCinemaAndDateRange(@Param("cinemaId") Integer cinemaId,
                                   @Param("from") LocalDateTime from,
                                   @Param("to") LocalDateTime to);

    /** Suất chiếu chưa diễn ra trong 1 phòng — dùng để chặn xóa phòng còn lịch sắp tới. */
    @Query("SELECT COUNT(st) FROM ShowTime st " +
            "WHERE st.room.id = :roomId " +
            "AND (st.isDeleted IS NULL OR st.isDeleted = false) " +
            "AND st.startTime >= :from")
    long countUpcomingByRoom(@Param("roomId") Integer roomId, @Param("from") LocalDateTime from);

    /** Suất chiếu chưa diễn ra trong toàn bộ phòng của 1 rạp. */
    @Query("SELECT COUNT(st) FROM ShowTime st " +
            "WHERE st.room.cinema.id = :cinemaId " +
            "AND (st.isDeleted IS NULL OR st.isDeleted = false) " +
            "AND (st.room.isDeleted IS NULL OR st.room.isDeleted = false) " +
            "AND st.startTime >= :from")
    long countUpcomingByCinema(@Param("cinemaId") Integer cinemaId, @Param("from") LocalDateTime from);

    /** Suất chiếu chưa diễn ra trong toàn bộ rạp của 1 thành phố. */
    @Query("SELECT COUNT(st) FROM ShowTime st " +
            "WHERE st.room.cinema.city.id = :cityId " +
            "AND (st.isDeleted IS NULL OR st.isDeleted = false) " +
            "AND (st.room.isDeleted IS NULL OR st.room.isDeleted = false) " +
            "AND (st.room.cinema.isDeleted IS NULL OR st.room.cinema.isDeleted = false) " +
            "AND st.startTime >= :from")
    long countUpcomingByCity(@Param("cityId") Integer cityId, @Param("from") LocalDateTime from);

    /** Dùng cho tạo mới — check trùng lịch trong cùng phòng */
    @Query("SELECT COUNT(st) FROM ShowTime st " +
            "WHERE st.room.id = :roomId " +
            "AND (st.isDeleted IS NULL OR st.isDeleted = false) " +
            "AND st.startTime < :endTime AND st.endTime > :startTime")
    Long countConflictingShowtimes(@Param("roomId") Integer roomId,
                                   @Param("startTime") LocalDateTime startTime,
                                   @Param("endTime") LocalDateTime endTime);

    /**
     * Dùng cho chỉnh sửa — check trùng lịch nhưng loại trừ chính suất chiếu đang edit.
     * Nếu không loại trừ, showtime sẽ tự conflict với chính nó.
     */
    @Query("SELECT COUNT(st) FROM ShowTime st " +
            "WHERE st.room.id = :roomId " +
            "AND st.id != :excludeId " +
            "AND (st.isDeleted IS NULL OR st.isDeleted = false) " +
            "AND st.startTime < :endTime AND st.endTime > :startTime")
    Long countConflictingShowtimesExcluding(@Param("roomId") Integer roomId,
                                            @Param("startTime") LocalDateTime startTime,
                                            @Param("endTime") LocalDateTime endTime,
                                            @Param("excludeId") Long excludeId);

    @Query("SELECT DISTINCT st.room.roomType FROM ShowTime st " +
            "WHERE st.movie.id = :movieId " +
            "AND (st.isDeleted IS NULL OR st.isDeleted = false) " +
            "AND (st.room.isDeleted IS NULL OR st.room.isDeleted = false)")
    List<String> findDistinctRoomTypesByMovieId(@Param("movieId") Integer movieId);
}