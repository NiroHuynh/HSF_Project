package com.hsf_project.dto.movie.response;

import com.hsf_project.entity.AgeRating;
import com.hsf_project.entity.MovieStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MovieResponse {
    Integer id;
    String title;
    String description;
    Integer durationMinutes;
    String director;
    String cast;
    LocalDate releaseDate;
    String posterUrl;
    AgeRating ageRating;
    BigDecimal averageRating;
    MovieStatus status;
    List<String> genres;
    LocalDateTime createdAt;
    String language;
    LocalDate endDate;
    Boolean isFeatured;
    String updatedBy;
    LocalDateTime updatedAt;
}
