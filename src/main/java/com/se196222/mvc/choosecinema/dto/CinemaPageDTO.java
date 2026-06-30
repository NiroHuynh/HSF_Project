package com.se196222.mvc.choosecinema.dto;

import java.util.List;

public class CinemaPageDTO {
    private Integer            cityId;
    private String             cityName;
    private List<CinemaCardDTO> cinemas;

    public CinemaPageDTO() {}

    public Integer getCityId() { return cityId; }
    public void setCityId(Integer cityId) { this.cityId = cityId; }

    public String getCityName() { return cityName; }
    public void setCityName(String cityName) { this.cityName = cityName; }

    public List<CinemaCardDTO> getCinemas() { return cinemas; }
    public void setCinemas(List<CinemaCardDTO> cinemas) { this.cinemas = cinemas; }
}
