package com.hsf_project.repository;

import com.hsf_project.entity.Booking;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserIdAndIsDeletedFalseOrderByBookingDateDesc(Long userId);

    Optional<Booking> findByBookingCodeAndIsDeletedFalse(String bookingCode);

    // Dùng cho Admin — lấy tất cả booking không lọc chi nhánh
    @EntityGraph(attributePaths = {
            "user",
            "tickets",
            "tickets.seat",
            "tickets.showtime",
            "tickets.showtime.movie",
            "tickets.showtime.room"
    })
    List<Booking> findByIsDeletedFalseOrderByBookingDateDesc();

    /**
     * Lấy booking theo chi nhánh của staff đang đăng nhập.
     * Lọc qua: booking → tickets → showtime → room → cinema.id
     *
     * Dùng JOIN FETCH thay vì @EntityGraph vì cần WHERE trên quan hệ
     * lồng nhau (room.cinema.id), @EntityGraph không hỗ trợ điều kiện WHERE.
     */
    @Query("SELECT DISTINCT b FROM Booking b " +
            "JOIN FETCH b.user " +
            "JOIN FETCH b.tickets t " +
            "JOIN FETCH t.seat " +
            "JOIN FETCH t.showtime st " +
            "JOIN FETCH st.movie " +
            "JOIN FETCH st.room r " +
            "WHERE r.cinema.id = :cinemaId " +
            "AND b.isDeleted = false " +
            "ORDER BY b.bookingDate DESC")
    List<Booking> findByCinemaIdAndIsDeletedFalse(@Param("cinemaId") Integer cinemaId);
}