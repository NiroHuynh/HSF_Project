package com.hsf_project.repository;

import com.hsf_project.entity.MovieReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovieReviewRepository extends JpaRepository<MovieReview, Integer> {
    List<MovieReview> findByMovieIdAndIsDeletedFalseOrderByCreatedAtDesc(Integer movieId);
}
