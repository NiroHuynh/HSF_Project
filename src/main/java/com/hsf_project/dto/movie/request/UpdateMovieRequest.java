package com.hsf_project.dto.movie.request;

import com.hsf_project.entity.enums.AgeRating;
import com.hsf_project.entity.enums.MovieStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateMovieRequest {
    @Size(max = 200, message = "Tên phim không được vượt quá 200 ký tự")
    String title;

    @Min(value = 30, message = "Thời lượng phải từ 30 đến 300 phút")
    @Max(value = 300, message = "Thời lượng phải từ 30 đến 300 phút")
    Integer durationMinutes;

    LocalDate releaseDate;

    @Size(max = 100, message = "Tên đạo diễn không được vượt quá 100 ký tự")
    String director;

    String cast;

    @Size(max = 5000, message = "Mô tả không được vượt quá 5000 ký tự")
    String description;

    AgeRating ageRating;

    List<@NotNull Integer> genreIds;

    String language;

    LocalDate endDate;

    Boolean isFeatured;

    MovieStatus status;
}
