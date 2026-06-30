package com.se196222.mvc.choosecinema.dto;

import java.util.List;
import java.util.Map;

public class MovieRowDTO {
    private Integer movieId;
    private String  title;
    private String  genreLabel;       // "Toi pham, Am nhac"
    private int     durationMinutes;
    private String  ageRating;        // "C", "T16", "P" …
    private double  averageRating;
    private String  posterUrl;

    // key = screen format ("2D" / "IMAX" / "4DX")
    // value = ordered list of time slots
    private Map<String, List<ShowTimeSlotDTO>> slotsByFormat;

    public MovieRowDTO() {}

    public Integer getMovieId() { return movieId; }
    public void setMovieId(Integer movieId) { this.movieId = movieId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getGenreLabel() { return genreLabel; }
    public void setGenreLabel(String genreLabel) { this.genreLabel = genreLabel; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

    public String getAgeRating() { return ageRating; }
    public void setAgeRating(String ageRating) { this.ageRating = ageRating; }

    public double getAverageRating() { return averageRating; }
    public void setAverageRating(double averageRating) { this.averageRating = averageRating; }

    public String getPosterUrl() { return posterUrl; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }

    public Map<String, List<ShowTimeSlotDTO>> getSlotsByFormat() { return slotsByFormat; }
    public void setSlotsByFormat(Map<String, List<ShowTimeSlotDTO>> slotsByFormat) {
        this.slotsByFormat = slotsByFormat;
    }
}
