package com.se196222.mvc.choosecinema.dto;

import java.util.List;

public class CinemaCardDTO {
    private Integer      cinemaId;
    private String       cinemaName;
    private String       address;
    private List<String> formats;    // ["2D", "IMAX"]  — for format chips
    private List<MovieRowDTO> movies;

    public CinemaCardDTO() {}

    public Integer getCinemaId() { return cinemaId; }
    public void setCinemaId(Integer cinemaId) { this.cinemaId = cinemaId; }

    public String getCinemaName() { return cinemaName; }
    public void setCinemaName(String cinemaName) { this.cinemaName = cinemaName; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public List<String> getFormats() { return formats; }
    public void setFormats(List<String> formats) { this.formats = formats; }

    public List<MovieRowDTO> getMovies() { return movies; }
    public void setMovies(List<MovieRowDTO> movies) { this.movies = movies; }
}
