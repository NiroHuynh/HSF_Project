package com.se196222.mvc.choosecinema.service;



import com.se196222.mvc.choosecinema.entity.Cinema;
import com.se196222.mvc.choosecinema.entity.City;
import com.se196222.mvc.choosecinema.entity.ShowTime;

import java.time.LocalDate;
import java.util.List;

public interface CinemaService {
    // Return all active cities for the city pill selector
    List<City> getAllCities();

    // Return all active cinemas in a city
    List<Cinema> getCinemasByCity(Integer cityId);

    // Return showtimes for a cinema on a specific date
    List<ShowTime> getShowtimes(Integer cinemaId, LocalDate date);

    // Return distinct screen formats supported by a cinema
    List<String> getFormatsByCinema(Integer cinemaId);
}
