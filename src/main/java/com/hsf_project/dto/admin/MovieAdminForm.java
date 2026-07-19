package com.hsf_project.dto.admin;

import com.hsf_project.entity.enums.AgeRating;
import com.hsf_project.entity.enums.MovieStatus;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

public class MovieAdminForm {
    private Integer id;
    @NotBlank @Size(max = 255) private String title;
    @Size(max = 4000) private String description;
    @NotNull @Min(1) @Max(600) private Integer durationMinutes;
    @Size(max = 100) private String director;
    @Size(max = 2000) private String cast;
    @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) private LocalDate releaseDate;
    @Size(max = 500) private String posterUrl;
    @NotNull private AgeRating ageRating;
    @NotNull private MovieStatus status;

    public Integer getId() { return id; } public void setId(Integer id) { this.id = id; }
    public String getTitle() { return title; } public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; } public void setDescription(String description) { this.description = description; }
    public Integer getDurationMinutes() { return durationMinutes; } public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
    public String getDirector() { return director; } public void setDirector(String director) { this.director = director; }
    public String getCast() { return cast; } public void setCast(String cast) { this.cast = cast; }
    public LocalDate getReleaseDate() { return releaseDate; } public void setReleaseDate(LocalDate releaseDate) { this.releaseDate = releaseDate; }
    public String getPosterUrl() { return posterUrl; } public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }
    public AgeRating getAgeRating() { return ageRating; } public void setAgeRating(AgeRating ageRating) { this.ageRating = ageRating; }
    public MovieStatus getStatus() { return status; } public void setStatus(MovieStatus status) { this.status = status; }
}
