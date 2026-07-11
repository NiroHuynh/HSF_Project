package com.hsf_project.repository.auth;

import com.hsf_project.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User findByEmailAndPasswordAndIsDeletedFalseAndStatus(String email, String password, String status);

    Optional<User> findByEmailAndIsDeletedFalse(String email);

    //Lấy thời gian khóa mới nhất của User bằng ID
    @Query("SELECT u.lockBookingUntil FROM User u WHERE u.id = :userId")
    LocalDateTime getLockBookingUntilByUserId(@Param("userId") Long userId);

    /**
     * Lấy danh sách khách hàng đã đặt vé tại cinema, kèm thống kê.
     *
     * Trả về Object[] gồm 3 phần tử:
     *   [0] User          — thông tin khách hàng
     *   [1] Long          — số booking tại cinema này
     *   [2] BigDecimal    — tổng finalAmount tại cinema này
     *
     * Lọc qua: Booking → tickets → showtime → room → cinema.id
     */
    @Query("SELECT u, COUNT(DISTINCT b), COALESCE(SUM(b.finalAmount), 0) " +
            "FROM User u " +
            "JOIN Booking b ON b.user = u " +
            "JOIN b.tickets t " +
            "JOIN t.showtime st " +
            "JOIN st.room r " +
            "WHERE r.cinema.id = :cinemaId " +
            "AND b.isDeleted = false " +
            "AND u.isDeleted = false " +
            "GROUP BY u " +
            "ORDER BY u.createdAt DESC")
    List<Object[]> findCustomersWithStatsByCinema(@Param("cinemaId") Integer cinemaId);
}
