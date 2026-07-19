package com.hsf_project.dto.movie.request;

import com.hsf_project.entity.AgeRating;
import com.hsf_project.entity.MovieStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
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
public class CreateMovieRequest {
    @NotBlank(message = "Tên phim không được để trống")
    @Size(max = 200, message = "Tên phim không được vượt quá 200 ký tự")
    String title;

    @NotNull(message = "Thời lượng không được để trống")
    @Min(value = 30, message = "Thời lượng phải từ 30 đến 300 phút")
    @Max(value = 300, message = "Thời lượng phải từ 30 đến 300 phút")
    Integer durationMinutes;

    @NotNull(message = "Ngày khởi chiếu không được để trống")
    LocalDate releaseDate;

    @NotBlank(message = "Đạo diễn không được để trống")
    @Size(max = 100, message = "Tên đạo diễn không được vượt quá 100 ký tự")
    String director;

    @NotBlank(message = "Diễn viên không được để trống")
    String cast;

    @NotBlank(message = "Mô tả phim không được để trống")
    @Size(max = 5000, message = "Mô tả không được vượt quá 5000 ký tự")
    String description;

    AgeRating ageRating;

    @NotEmpty(message = "Phải chọn ít nhất một thể loại")
    List<@NotNull Integer> genreIds;

    @NotBlank(message = "Ngôn ngữ không được để trống")
    String language;

    @NotNull(message = "Ngày kết thúc dự kiến không được để trống")
    LocalDate endDate;

    Boolean isFeatured;

    MovieStatus status;
}
