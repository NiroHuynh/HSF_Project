package com.se196222.mvc.choosecinema.service.impl;

import com.se196222.mvc.choosecinema.entity.Cinema;
import com.se196222.mvc.choosecinema.entity.City;
import com.se196222.mvc.choosecinema.entity.ShowTime;
import com.se196222.mvc.choosecinema.repository.CinemaRepository;
import com.se196222.mvc.choosecinema.repository.CinemaRoomRepository;
import com.se196222.mvc.choosecinema.repository.CityRepository;
import com.se196222.mvc.choosecinema.repository.ShowTimeRepository;
import com.se196222.mvc.choosecinema.service.CinemaService;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CinemaServiceImpl implements CinemaService {

    private final CityRepository       cityRepository;
    private final CinemaRepository     cinemaRepository;
    private final CinemaRoomRepository cinemaRoomRepository;
    private final ShowTimeRepository   showTimeRepository;

    // Constructor injection (no Lombok — follows exam requirement)
    public CinemaServiceImpl(CityRepository cityRepository,
                             CinemaRepository cinemaRepository,
                             CinemaRoomRepository cinemaRoomRepository,
                             ShowTimeRepository showTimeRepository) {
        this.cityRepository       = cityRepository;
        this.cinemaRepository     = cinemaRepository;
        this.cinemaRoomRepository = cinemaRoomRepository;
        this.showTimeRepository   = showTimeRepository;
    }

    @Override
    public List<City> getAllCities() {
        return cityRepository.findByIsDeletedFalseOrderByName();
    }

    @Override
    public List<Cinema> getCinemasByCity(Integer cityId) {
        return cinemaRepository.findByCityCityIdAndIsDeletedFalseOrderByName(cityId);
    }

    @Override
    public List<ShowTime> getShowtimes(Integer cinemaId, LocalDate date) {
        return showTimeRepository.findByCinemaIdAndDate(cinemaId, date);
    }

    @Override
    public List<String> getFormatsByCinema(Integer cinemaId) {
        List<String> rawTypes = cinemaRoomRepository.findDistinctRoomTypesByCinemaId(cinemaId);

        // Map room_type → display format, preserve display order
        List<String> order = Arrays.asList("2D", "3D", "IMAX", "4DX", "VIP");

        return rawTypes.stream()
                .map(rt -> switch (rt.toUpperCase()) {
                    case "IMAX"     -> "IMAX";
                    case "4DX"      -> "4DX";
                    case "VIP"      -> "VIP";
                    default         -> "2D";
                })
                .distinct()
                .sorted((a, b) -> order.indexOf(a) - order.indexOf(b))
                .collect(Collectors.toList());
    }
}
