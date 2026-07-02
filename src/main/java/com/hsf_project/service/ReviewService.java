package com.hsf_project.service;

import com.hsf_project.dto.ReviewResponse;

import java.util.List;

public interface ReviewService {
    void createReview(Integer movieId, Long userId, Short ratingStar, String comment);
    List<ReviewResponse> getReviewsByMovieId(Integer movieId);
    boolean hasUserReviewed(Integer movieId, Long userId);
}
