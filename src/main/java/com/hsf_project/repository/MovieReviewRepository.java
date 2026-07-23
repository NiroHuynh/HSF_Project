package com.hsf_project.repository;

import com.hsf_project.entity.MovieReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface MovieReviewRepository extends JpaRepository<MovieReview, Integer> {
    List<MovieReview> findByMovieIdAndIsDeletedFalseOrderByCreatedAtDesc(Integer movieId);

    Optional<MovieReview> findByMovieIdAndUserIdAndIsDeletedFalse(Integer movieId, Long userId);

    @Query("SELECT AVG(r.ratingStar) FROM MovieReview r WHERE r.movie.id = :movieId AND r.isDeleted = false")
    BigDecimal findAverageRatingByMovieId(@Param("movieId") Integer movieId);

    @Query("SELECT DISTINCT r.movie.id FROM MovieReview r WHERE r.isDeleted = false")
    List<Integer> findDistinctMovieIdsWithReviews();
}
