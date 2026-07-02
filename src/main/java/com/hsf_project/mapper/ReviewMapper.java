package com.hsf_project.mapper;

import com.hsf_project.dto.ReviewResponse;
import com.hsf_project.dto.request.CreateReviewRequest;
import com.hsf_project.entity.Movie;
import com.hsf_project.entity.MovieReview;
import com.hsf_project.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    @Mapping(target = "userName", expression = "java(review.getUser().getFullName())")
    @Mapping(target = "userAvatar", expression = "java(String.valueOf(review.getUser().getFullName().charAt(0)))")
    @Mapping(target = "formattedCreatedAt", expression = "java(formatDate(review))")
    ReviewResponse toReviewDTO(MovieReview review);

    List<ReviewResponse> toReviewDTOList(List<MovieReview> reviews);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "isDeleted", expression = "java(Boolean.FALSE)")
    MovieReview toMovieReview(CreateReviewRequest request, Movie movie, User user);

    default String formatDate(MovieReview review) {
        if (review.getCreatedAt() == null) return "";
        return review.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
}
