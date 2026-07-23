package com.hsf_project.service.impl;

import com.hsf_project.dto.request.CreateReviewRequest;
import com.hsf_project.dto.ReviewResponse;
import com.hsf_project.entity.Movie;
import com.hsf_project.entity.MovieReview;
import com.hsf_project.entity.User;
import com.hsf_project.exception.AppException;
import com.hsf_project.exception.ErrorCode;
import com.hsf_project.mapper.ReviewMapper;
import com.hsf_project.repository.MovieReviewRepository;
import com.hsf_project.repository.MovieRepository;
import com.hsf_project.service.ReviewService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
public class ReviewServiceImpl implements ReviewService {

    @Autowired
    private MovieReviewRepository movieReviewRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private ReviewMapper reviewMapper;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void createReview(Integer movieId, Long userId, Short ratingStar, String comment) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new AppException(ErrorCode.MOVIE_NOT_FOUND));

        User user = entityManager.getReference(User.class, userId);

        Optional<MovieReview> existing = movieReviewRepository
                .findByMovieIdAndUserIdAndIsDeletedFalse(movieId, userId);
        if (existing.isPresent()) {
            throw new AppException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }

        CreateReviewRequest request = new CreateReviewRequest();
        request.setRatingStar(ratingStar);
        request.setComment(comment);

        MovieReview review = reviewMapper.toMovieReview(request, movie, user);
        movieReviewRepository.save(review);

        updateAverageRating(movieId);
    }

    @Override
    public List<ReviewResponse> getReviewsByMovieId(Integer movieId) {
        List<MovieReview> reviews = movieReviewRepository
                .findByMovieIdAndIsDeletedFalseOrderByCreatedAtDesc(movieId);
        return reviewMapper.toReviewDTOList(reviews);
    }

    @Override
    public boolean hasUserReviewed(Integer movieId, Long userId) {
        return movieReviewRepository
                .findByMovieIdAndUserIdAndIsDeletedFalse(movieId, userId)
                .isPresent();
    }

    @Override
    public void recalculateRating(Integer movieId) {
        updateAverageRating(movieId);
    }

    private void updateAverageRating(Integer movieId) {
        BigDecimal avg = movieReviewRepository.findAverageRatingByMovieId(movieId);
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new AppException(ErrorCode.MOVIE_NOT_FOUND));
        if (avg != null) {
            movie.setAverageRating(avg.setScale(1, RoundingMode.HALF_UP));
        } else {
            movie.setAverageRating(BigDecimal.ZERO);
        }
        movieRepository.save(movie);
    }
}
