package com.hsf_project.repository.movie;

import com.hsf_project.entity.Movie;
import com.hsf_project.entity.MovieStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface MovieRepository extends JpaRepository<Movie, Integer> {

    /**
     * 1. Dành cho PHIM ĐANG CHIẾU:
     * Lọc theo trạng thái NOW_SHOWING, chưa bị xóa (isDeleted = false hoặc null).
     * Sắp xếp theo điểm Rating giảm dần (từ cao xuống thấp).
     * Giới hạn lấy đúng 4 phần tử (Top 4).
     */
    @Query(value = "SELECT TOP 4 * FROM movie m " +
            "WHERE m.status = :status " +
            "AND (m.is_deleted = 0 OR m.is_deleted IS NULL) " +
            "ORDER BY m.average_rating DESC", nativeQuery = true)
    List<Movie> findTopMoviesByRating(@Param("status") String status);

    /**
     * 2. Dành cho PHIM SẮP CHIẾU:
     * Lọc theo trạng thái COMING_SOON, chưa bị xóa, ngày khởi chiếu lớn hơn hoặc bằng ngày hiện tại.
     * Sắp xếp theo ngày khởi chiếu tăng dần (ngày gần hiện tại nhất lên đầu).
     * Giới hạn lấy đúng 4 phần tử (Top 4).
     */
    @Query(value = "SELECT TOP 4 * FROM movie m " +
            "WHERE m.status = :status " +
            "AND (m.is_deleted = 0 OR m.is_deleted IS NULL) " +
            "AND m.release_date >= :currentDate " +
            "ORDER BY m.release_date ASC", nativeQuery = true)
    List<Movie> findUpcomingMoviesByReleaseDate(
            @Param("status") String status,
            @Param("currentDate") LocalDate currentDate
    );

    Page<Movie> findByStatusAndIsDeletedFalse(MovieStatus status, Pageable pageable);

    Page<Movie> findByTitleContainingIgnoreCaseAndStatusAndIsDeletedFalse(
            String title, MovieStatus status, Pageable pageable);
}
