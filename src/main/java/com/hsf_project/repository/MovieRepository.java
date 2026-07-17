package com.hsf_project.repository;

import com.hsf_project.entity.Movie;
import com.hsf_project.entity.enums.MovieStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface MovieRepository extends JpaRepository<Movie, Integer> {

    @Query(value = "SELECT TOP 4 * FROM movie m " +
            "WHERE m.status = :status " +
            "AND (m.is_deleted = 0 OR m.is_deleted IS NULL) " +
            "ORDER BY m.average_rating DESC", nativeQuery = true)
    List<Movie> findTopMoviesByRating(@Param("status") String status);

    @Query(value = "SELECT TOP 4 * FROM movie m " +
            "WHERE m.status = :status " +
            "AND (m.is_deleted = 0 OR m.is_deleted IS NULL) " +
            "AND m.release_date >= :currentDate " +
            "ORDER BY m.release_date ASC", nativeQuery = true)
    List<Movie> findUpcomingMoviesByReleaseDate(
            @Param("status") String status,
            @Param("currentDate") LocalDate currentDate);

    Page<Movie> findByStatusAndIsDeletedFalse(MovieStatus status, Pageable pageable);

    Page<Movie> findByTitleContainingIgnoreCaseAndStatusAndIsDeletedFalse(
            String title, MovieStatus status, Pageable pageable);

    @Query("SELECT DISTINCT m FROM Movie m JOIN m.genres g WHERE g.id IN :genreIds AND m.status = :status AND (m.isDeleted IS NULL OR m.isDeleted = false)")
    Page<Movie> findByGenreIdsAndStatus(@Param("genreIds") List<Integer> genreIds, @Param("status") MovieStatus status, Pageable pageable);

    @Query("SELECT DISTINCT m FROM Movie m JOIN m.genres g WHERE g.id IN :genreIds AND m.status = :status AND (m.isDeleted IS NULL OR m.isDeleted = false) AND LOWER(m.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Movie> findByGenreIdsAndStatusAndTitleContaining(@Param("genreIds") List<Integer> genreIds, @Param("status") MovieStatus status, @Param("keyword") String keyword, Pageable pageable);

    /**
     * Lấy danh sách phim cho dropdown tạo suất chiếu.
     * Chỉ lấy phim NOW_SHOWING và COMING_SOON, sắp xếp theo tên.
     */
    @Query("SELECT m FROM Movie m WHERE m.status IN :statuses " +
            "AND (m.isDeleted IS NULL OR m.isDeleted = false) " +
            "ORDER BY m.title ASC")
    List<Movie> findByStatusInForDropdown(@Param("statuses") List<MovieStatus> statuses);
}