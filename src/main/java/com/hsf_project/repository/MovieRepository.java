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

    Page<Movie> findDistinctByGenres_IdInAndStatusAndIsDeletedFalse(List<Integer> genreIds, MovieStatus status, Pageable pageable);

    /**
     * Lấy danh sách phim cho dropdown tạo suất chiếu.
     * Chỉ lấy phim NOW_SHOWING và COMING_SOON, sắp xếp theo tên.
     */
    @Query("SELECT m FROM Movie m WHERE m.status IN :statuses " +
            "AND (m.isDeleted IS NULL OR m.isDeleted = false) " +
            "ORDER BY m.title ASC")
    List<Movie> findByStatusInForDropdown(@Param("statuses") List<MovieStatus> statuses);

    boolean existsByTitleIgnoreCaseAndIsDeletedFalse(String title);

    java.util.Optional<Movie> findByTitleIgnoreCaseAndIsDeletedFalse(String title);

    long countByIsDeletedFalseOrIsDeletedNull();

    long countByStatusAndIsDeletedFalseOrIsDeletedNull(MovieStatus status);

    List<Movie> findByIsDeletedFalseOrIsDeletedNull();

    List<Movie> findByStatusAndIsDeletedFalse(MovieStatus status);

    List<Movie> findByStatusAndReleaseDateLessThanEqualAndIsDeletedFalse(MovieStatus status, LocalDate today);

    List<Movie> findByStatusAndEndDateBeforeAndIsDeletedFalse(MovieStatus status, LocalDate today);

    List<Movie> findByStatusAndEndDateIsNullAndIsDeletedFalse(MovieStatus status);

    @Query("SELECT COUNT(m) > 0 FROM Movie m WHERE LOWER(m.title) = LOWER(:title) AND YEAR(m.releaseDate) = :year AND (m.isDeleted IS NULL OR m.isDeleted = false)")
    boolean existsByTitleAndReleaseYear(@Param("title") String title, @Param("year") int year);
}
