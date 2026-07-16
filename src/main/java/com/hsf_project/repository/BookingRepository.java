package com.hsf_project.repository;

import com.hsf_project.entity.Booking;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserIdAndIsDeletedFalseOrderByBookingDateDesc(Long userId);

    Optional<Booking> findByBookingCodeAndIsDeletedFalse(String bookingCode);

    List<Booking> findByStatusAndExpiredAtBeforeAndIsDeletedFalse(String pending, LocalDateTime now);

    long countByUserIdAndIsDeletedFalseAndStatusAndNoteAndBookingDateAfter(Long userId, String status, String note, LocalDateTime bookingDate);
// ── manager/tickets ───────────────────────────────────────────────────────

    @Query("SELECT b FROM Booking b WHERE b.status IN ('CONFIRMED', 'EXPORTED') AND (b.isDeleted IS NULL OR b.isDeleted = false) AND b.bookingDate >= :from AND b.bookingDate < :to")
    List<Booking> findPaidInRange(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    /* ================== Aggregate cho dashboard doanh thu ================== */

    /** Tổng doanh thu thực thu (final_amount) của các booking đã thanh toán trong [from, to). */
    @Query("SELECT COALESCE(SUM(b.finalAmount), 0) FROM Booking b " +
            "WHERE b.status = 'PAID' AND (b.isDeleted IS NULL OR b.isDeleted = false) " +
            "AND b.bookingDate >= :from AND b.bookingDate < :to")
    java.math.BigDecimal sumRevenue(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    /** Số booking đã thanh toán trong [from, to). */
    @Query("SELECT COUNT(b) FROM Booking b " +
            "WHERE b.status = 'PAID' AND (b.isDeleted IS NULL OR b.isDeleted = false) " +
            "AND b.bookingDate >= :from AND b.bookingDate < :to")
    long countPaid(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    /** Doanh thu theo từng tháng của một năm: mỗi phần tử = [tháng (1-12), tổng doanh thu]. */
    @Query("SELECT FUNCTION('MONTH', b.bookingDate), COALESCE(SUM(b.finalAmount), 0) FROM Booking b " +
            "WHERE b.status = 'PAID' AND (b.isDeleted IS NULL OR b.isDeleted = false) " +
            "AND FUNCTION('YEAR', b.bookingDate) = :year " +
            "GROUP BY FUNCTION('MONTH', b.bookingDate)")
    List<Object[]> monthlyRevenue(@Param("year") int year);

    /**
     * Booking đã thanh toán kèm rạp (mọi vé trong 1 booking cùng suất chiếu nên cùng rạp).
     * DISTINCT để mỗi booking chỉ ra 1 dòng dù có nhiều vé — tổng hợp SUM làm ở tầng service
     * để tránh doanh thu bị nhân bản theo số vé.
     * Mỗi phần tử = [cinemaId, cinemaName, bookingId, finalAmount].
     */
    @Query("SELECT DISTINCT c.id, c.name, b.id, b.finalAmount FROM Booking b " +
            "JOIN b.tickets t JOIN t.showtime st JOIN st.room r JOIN r.cinema c " +
            "WHERE b.status = 'PAID' AND (b.isDeleted IS NULL OR b.isDeleted = false) " +
            "AND b.bookingDate >= :from AND b.bookingDate < :to")
    List<Object[]> paidBookingsWithCinema(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    /* ---------- Bản giới hạn theo rạp (trang Manager) ---------- */

    @Query("SELECT COALESCE(SUM(b.finalAmount), 0) FROM Booking b " +
            "WHERE b.status = 'PAID' AND (b.isDeleted IS NULL OR b.isDeleted = false) " +
            "AND b.bookingDate >= :from AND b.bookingDate < :to " +
            "AND EXISTS (SELECT 1 FROM Ticket t WHERE t.booking = b AND t.showtime.room.cinema.id = :cinemaId)")
    java.math.BigDecimal sumRevenueByCinema(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to,
                                            @Param("cinemaId") Integer cinemaId);

    @Query("SELECT COUNT(b) FROM Booking b " +
            "WHERE b.status = 'PAID' AND (b.isDeleted IS NULL OR b.isDeleted = false) " +
            "AND b.bookingDate >= :from AND b.bookingDate < :to " +
            "AND EXISTS (SELECT 1 FROM Ticket t WHERE t.booking = b AND t.showtime.room.cinema.id = :cinemaId)")
    long countPaidByCinema(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to,
                           @Param("cinemaId") Integer cinemaId);

    @Query("SELECT FUNCTION('MONTH', b.bookingDate), COALESCE(SUM(b.finalAmount), 0) FROM Booking b " +
            "WHERE b.status = 'PAID' AND (b.isDeleted IS NULL OR b.isDeleted = false) " +
            "AND FUNCTION('YEAR', b.bookingDate) = :year " +
            "AND EXISTS (SELECT 1 FROM Ticket t WHERE t.booking = b AND t.showtime.room.cinema.id = :cinemaId) " +
            "GROUP BY FUNCTION('MONTH', b.bookingDate)")
    List<Object[]> monthlyRevenueByCinema(@Param("year") int year, @Param("cinemaId") Integer cinemaId);
    @EntityGraph(attributePaths = {
            "user", "tickets", "tickets.seat",
            "tickets.showtime", "tickets.showtime.movie", "tickets.showtime.room"
    })
    List<Booking> findByIsDeletedFalseOrderByBookingDateDesc();

    @Query("SELECT DISTINCT b FROM Booking b " +
            "JOIN FETCH b.user JOIN FETCH b.tickets t JOIN FETCH t.seat " +
            "JOIN FETCH t.showtime st JOIN FETCH st.movie JOIN FETCH st.room r " +
            "WHERE r.cinema.id = :cinemaId AND b.isDeleted = false " +
            "ORDER BY b.bookingDate DESC")
    List<Booking> findByCinemaIdAndIsDeletedFalse(@Param("cinemaId") Integer cinemaId);

// ── manager/dashboard — thống kê tổng ────────────────────────────────────

    @Query(value = "SELECT COALESCE(SUM(b.final_amount), 0) FROM booking b " +
            "JOIN ticket t ON t.booking_id = b.id " +
            "JOIN show_time st ON st.id = t.showtime_id " +
            "JOIN cinema_room r ON r.id = st.room_id " +
            "WHERE r.cinema_id = :cinemaId AND b.is_deleted = 0 " +
            "AND b.status NOT IN ('PENDING', 'CANCELED') " +
            "AND b.booking_date BETWEEN :from AND :to", nativeQuery = true)
    BigDecimal getTotalRevenueByCinema(@Param("cinemaId") Integer cinemaId,
                                       @Param("from") LocalDateTime from,
                                       @Param("to") LocalDateTime to);

    @Query(value = "SELECT COUNT(DISTINCT b.id) FROM booking b " +
            "JOIN ticket t ON t.booking_id = b.id " +
            "JOIN show_time st ON st.id = t.showtime_id " +
            "JOIN cinema_room r ON r.id = st.room_id " +
            "WHERE r.cinema_id = :cinemaId AND b.is_deleted = 0 " +
            "AND b.booking_date BETWEEN :from AND :to", nativeQuery = true)
    Long countBookingsByCinema(@Param("cinemaId") Integer cinemaId,
                               @Param("from") LocalDateTime from,
                               @Param("to") LocalDateTime to);

    @Query(value = "SELECT COUNT(DISTINCT b.user_id) FROM booking b " +
            "JOIN ticket t ON t.booking_id = b.id " +
            "JOIN show_time st ON st.id = t.showtime_id " +
            "JOIN cinema_room r ON r.id = st.room_id " +
            "WHERE r.cinema_id = :cinemaId AND b.is_deleted = 0 " +
            "AND b.booking_date BETWEEN :from AND :to", nativeQuery = true)
    Long countCustomersByCinema(@Param("cinemaId") Integer cinemaId,
                                @Param("from") LocalDateTime from,
                                @Param("to") LocalDateTime to);

    @Query(value = "SELECT MONTH(b.booking_date) AS mon, COALESCE(SUM(b.final_amount), 0) AS rev " +
            "FROM booking b JOIN ticket t ON t.booking_id = b.id " +
            "JOIN show_time st ON st.id = t.showtime_id " +
            "JOIN cinema_room r ON r.id = st.room_id " +
            "WHERE r.cinema_id = :cinemaId AND b.is_deleted = 0 " +
            "AND b.status NOT IN ('PENDING', 'CANCELED') " +
            "AND b.booking_date BETWEEN :from AND :to " +
            "GROUP BY MONTH(b.booking_date) ORDER BY MONTH(b.booking_date)", nativeQuery = true)
    List<Object[]> getMonthlyRevenueByCinema(@Param("cinemaId") Integer cinemaId,
                                             @Param("from") LocalDateTime from,
                                             @Param("to") LocalDateTime to);

    @Query(value = "SELECT " +
            "CASE WHEN DAY(b.booking_date) <= 7 THEN 1 " +
            "     WHEN DAY(b.booking_date) <= 14 THEN 2 " +
            "     WHEN DAY(b.booking_date) <= 21 THEN 3 ELSE 4 END AS week_num, " +
            "COALESCE(SUM(b.final_amount), 0) AS rev " +
            "FROM booking b JOIN ticket t ON t.booking_id = b.id " +
            "JOIN show_time st ON st.id = t.showtime_id " +
            "JOIN cinema_room r ON r.id = st.room_id " +
            "WHERE r.cinema_id = :cinemaId AND b.is_deleted = 0 " +
            "AND b.status NOT IN ('PENDING', 'CANCELED') " +
            "AND b.booking_date BETWEEN :from AND :to " +
            "GROUP BY CASE WHEN DAY(b.booking_date) <= 7 THEN 1 " +
            "              WHEN DAY(b.booking_date) <= 14 THEN 2 " +
            "              WHEN DAY(b.booking_date) <= 21 THEN 3 ELSE 4 END " +
            "ORDER BY week_num", nativeQuery = true)
    List<Object[]> getWeeklyRevenueByCinema(@Param("cinemaId") Integer cinemaId,
                                            @Param("from") LocalDateTime from,
                                            @Param("to") LocalDateTime to);

    @Query(value = "SELECT " +
            "CASE WHEN MONTH(b.booking_date) BETWEEN 1 AND 3 THEN 1 " +
            "     WHEN MONTH(b.booking_date) BETWEEN 4 AND 6 THEN 2 " +
            "     WHEN MONTH(b.booking_date) BETWEEN 7 AND 9 THEN 3 ELSE 4 END AS quarter_num, " +
            "COALESCE(SUM(b.final_amount), 0) AS rev " +
            "FROM booking b JOIN ticket t ON t.booking_id = b.id " +
            "JOIN show_time st ON st.id = t.showtime_id " +
            "JOIN cinema_room r ON r.id = st.room_id " +
            "WHERE r.cinema_id = :cinemaId AND b.is_deleted = 0 " +
            "AND b.status NOT IN ('PENDING', 'CANCELED') " +
            "AND b.booking_date BETWEEN :from AND :to " +
            "GROUP BY CASE WHEN MONTH(b.booking_date) BETWEEN 1 AND 3 THEN 1 " +
            "              WHEN MONTH(b.booking_date) BETWEEN 4 AND 6 THEN 2 " +
            "              WHEN MONTH(b.booking_date) BETWEEN 7 AND 9 THEN 3 ELSE 4 END " +
            "ORDER BY quarter_num", nativeQuery = true)
    List<Object[]> getQuarterlyRevenueByCinema(@Param("cinemaId") Integer cinemaId,
                                               @Param("from") LocalDateTime from,
                                               @Param("to") LocalDateTime to);

    @Query("SELECT DISTINCT b FROM Booking b " +
            "JOIN FETCH b.user JOIN FETCH b.tickets t JOIN FETCH t.seat " +
            "JOIN FETCH t.showtime st JOIN FETCH st.movie JOIN FETCH st.room r " +
            "LEFT JOIN FETCH b.promotion " +
            "WHERE b.bookingCode = :code AND r.cinema.id = :cinemaId AND b.isDeleted = false")
    Optional<Booking> findByCodeAndCinemaForSearch(@Param("code") String code,
                                                   @Param("cinemaId") Integer cinemaId);


// ── manager/revenue — thống kê chi tiết vé + combo ───────────────────────

    /** Đếm tổng số vé lẻ đã bán (không phải số booking) */
    @Query(value = "SELECT COUNT(DISTINCT t.id) FROM ticket t " +
            "JOIN show_time st ON st.id = t.showtime_id " +
            "JOIN cinema_room r ON r.id = st.room_id " +
            "JOIN booking b ON b.id = t.booking_id " +
            "WHERE r.cinema_id = :cinemaId AND b.is_deleted = 0 " +
            "AND b.booking_date BETWEEN :from AND :to", nativeQuery = true)
    Long countTicketsSoldByCinema(@Param("cinemaId") Integer cinemaId,
                                  @Param("from") LocalDateTime from,
                                  @Param("to") LocalDateTime to);

    /** Tổng doanh thu từ combo tại chi nhánh */
    @Query(value = "SELECT COALESCE(SUM(bc.total_price), 0) FROM booking_combo bc " +
            "WHERE bc.booking_id IN ( " +
            "    SELECT DISTINCT b.id FROM booking b " +
            "    JOIN ticket t ON t.booking_id = b.id " +
            "    JOIN show_time st ON st.id = t.showtime_id " +
            "    JOIN cinema_room r ON r.id = st.room_id " +
            "    WHERE r.cinema_id = :cinemaId AND b.is_deleted = 0 " +
            "    AND b.booking_date BETWEEN :from AND :to)", nativeQuery = true)
    BigDecimal getTotalComboRevenueByCinema(@Param("cinemaId") Integer cinemaId,
                                            @Param("from") LocalDateTime from,
                                            @Param("to") LocalDateTime to);

    /** Đếm tổng số combo đã bán */
    @Query(value = "SELECT COALESCE(SUM(bc.quantity), 0) FROM booking_combo bc " +
            "WHERE bc.booking_id IN ( " +
            "    SELECT DISTINCT b.id FROM booking b " +
            "    JOIN ticket t ON t.booking_id = b.id " +
            "    JOIN show_time st ON st.id = t.showtime_id " +
            "    JOIN cinema_room r ON r.id = st.room_id " +
            "    WHERE r.cinema_id = :cinemaId AND b.is_deleted = 0 " +
            "    AND b.booking_date BETWEEN :from AND :to)", nativeQuery = true)
    Long countCombosSoldByCinema(@Param("cinemaId") Integer cinemaId,
                                 @Param("from") LocalDateTime from,
                                 @Param("to") LocalDateTime to);

    /** Doanh thu combo theo tháng — cho biểu đồ stacked year/quarter mode */
    @Query(value = "SELECT MONTH(b.booking_date) AS mon, COALESCE(SUM(bc.total_price), 0) AS rev " +
            "FROM booking b JOIN booking_combo bc ON bc.booking_id = b.id " +
            "WHERE b.id IN ( " +
            "    SELECT DISTINCT b2.id FROM booking b2 " +
            "    JOIN ticket t ON t.booking_id = b2.id " +
            "    JOIN show_time st ON st.id = t.showtime_id " +
            "    JOIN cinema_room r ON r.id = st.room_id " +
            "    WHERE r.cinema_id = :cinemaId AND b2.is_deleted = 0 " +
            "    AND b2.booking_date BETWEEN :from AND :to) " +
            "GROUP BY MONTH(b.booking_date) ORDER BY MONTH(b.booking_date)", nativeQuery = true)
    List<Object[]> getMonthlyComboRevenueByCinema(@Param("cinemaId") Integer cinemaId,
                                                  @Param("from") LocalDateTime from,
                                                  @Param("to") LocalDateTime to);

    /** Doanh thu combo theo tuần — cho biểu đồ stacked month mode */
    @Query(value = "SELECT " +
            "CASE WHEN DAY(b.booking_date) <= 7 THEN 1 " +
            "     WHEN DAY(b.booking_date) <= 14 THEN 2 " +
            "     WHEN DAY(b.booking_date) <= 21 THEN 3 ELSE 4 END AS week_num, " +
            "COALESCE(SUM(bc.total_price), 0) AS rev " +
            "FROM booking b JOIN booking_combo bc ON bc.booking_id = b.id " +
            "WHERE b.id IN ( " +
            "    SELECT DISTINCT b2.id FROM booking b2 " +
            "    JOIN ticket t ON t.booking_id = b2.id " +
            "    JOIN show_time st ON st.id = t.showtime_id " +
            "    JOIN cinema_room r ON r.id = st.room_id " +
            "    WHERE r.cinema_id = :cinemaId AND b2.is_deleted = 0 " +
            "    AND b2.booking_date BETWEEN :from AND :to) " +
            "GROUP BY CASE WHEN DAY(b.booking_date) <= 7 THEN 1 " +
            "              WHEN DAY(b.booking_date) <= 14 THEN 2 " +
            "              WHEN DAY(b.booking_date) <= 21 THEN 3 ELSE 4 END " +
            "ORDER BY week_num", nativeQuery = true)
    List<Object[]> getWeeklyComboRevenueByCinema(@Param("cinemaId") Integer cinemaId,
                                                 @Param("from") LocalDateTime from,
                                                 @Param("to") LocalDateTime to);

    /** Doanh thu combo theo quý — cho biểu đồ stacked quarter mode */
    @Query(value = "SELECT " +
            "CASE WHEN MONTH(b.booking_date) BETWEEN 1 AND 3 THEN 1 " +
            "     WHEN MONTH(b.booking_date) BETWEEN 4 AND 6 THEN 2 " +
            "     WHEN MONTH(b.booking_date) BETWEEN 7 AND 9 THEN 3 ELSE 4 END AS quarter_num, " +
            "COALESCE(SUM(bc.total_price), 0) AS rev " +
            "FROM booking b JOIN booking_combo bc ON bc.booking_id = b.id " +
            "WHERE b.id IN ( " +
            "    SELECT DISTINCT b2.id FROM booking b2 " +
            "    JOIN ticket t ON t.booking_id = b2.id " +
            "    JOIN show_time st ON st.id = t.showtime_id " +
            "    JOIN cinema_room r ON r.id = st.room_id " +
            "    WHERE r.cinema_id = :cinemaId AND b2.is_deleted = 0 " +
            "    AND b2.booking_date BETWEEN :from AND :to) " +
            "GROUP BY CASE WHEN MONTH(b.booking_date) BETWEEN 1 AND 3 THEN 1 " +
            "              WHEN MONTH(b.booking_date) BETWEEN 4 AND 6 THEN 2 " +
            "              WHEN MONTH(b.booking_date) BETWEEN 7 AND 9 THEN 3 ELSE 4 END " +
            "ORDER BY quarter_num", nativeQuery = true)
    List<Object[]> getQuarterlyComboRevenueByCinema(@Param("cinemaId") Integer cinemaId,
                                                    @Param("from") LocalDateTime from,
                                                    @Param("to") LocalDateTime to);

    /**
     * Top 5 phim doanh thu cao nhất tại chi nhánh.
     * Dùng CTE để tránh double-count final_amount khi 1 booking có nhiều vé.
     * Trả về Object[]: [0]=title, [1]=ticket_count, [2]=total_revenue
     */
    @Query(value = "WITH booking_per_movie AS ( " +
            "    SELECT DISTINCT b.id, st.movie_id, b.final_amount " +
            "    FROM booking b " +
            "    JOIN ticket t ON t.booking_id = b.id " +
            "    JOIN show_time st ON st.id = t.showtime_id " +
            "    JOIN cinema_room r ON r.id = st.room_id " +
            "    WHERE r.cinema_id = :cinemaId AND b.is_deleted = 0 " +
            "    AND b.booking_date BETWEEN :from AND :to " +
            "), ticket_per_movie AS ( " +
            "    SELECT st.movie_id, COUNT(t.id) AS cnt " +
            "    FROM ticket t " +
            "    JOIN show_time st ON st.id = t.showtime_id " +
            "    JOIN booking b ON b.id = t.booking_id " +
            "    JOIN cinema_room r ON r.id = st.room_id " +
            "    WHERE r.cinema_id = :cinemaId AND b.is_deleted = 0 " +
            "    AND b.booking_date BETWEEN :from AND :to " +
            "    GROUP BY st.movie_id " +
            ") " +
            "SELECT TOP 5 m.title, tm.cnt AS ticket_count, " +
            "    COALESCE(SUM(bpm.final_amount), 0) AS total_revenue " +
            "FROM booking_per_movie bpm " +
            "JOIN movie m ON m.id = bpm.movie_id " +
            "JOIN ticket_per_movie tm ON tm.movie_id = bpm.movie_id " +
            "GROUP BY m.id, m.title, tm.cnt " +
            "ORDER BY total_revenue DESC", nativeQuery = true)
    List<Object[]> getTopMoviesByCinema(@Param("cinemaId") Integer cinemaId,
                                        @Param("from") LocalDateTime from,
                                        @Param("to") LocalDateTime to);

    List<Booking> findByBookingDateBetweenAndStatusAndIsDeletedFalse(
            LocalDateTime from, LocalDateTime to, String status);

    List<Booking> findByUserIdAndIsDeletedFalse(Long userId);

    List<Booking> findByBookingDateBetweenAndIsDeletedFalse(LocalDateTime from, LocalDateTime to);

    List<Booking> findByIsDeletedFalse();
}
