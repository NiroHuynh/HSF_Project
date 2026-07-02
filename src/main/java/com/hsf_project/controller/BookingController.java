package com.hsf_project.controller;

import com.hsf_project.dto.response.CinemaScheduleResponse;
import com.hsf_project.engine.ShowDateEngine;
import com.hsf_project.entity.Cinema;
import com.hsf_project.entity.City;
import com.hsf_project.entity.Movie;
import com.hsf_project.service.CinemaService;
import com.hsf_project.service.CityService;
import com.hsf_project.service.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * DEMO Controller — chỉ để TEST render trang movie-detail.html trong browser.
 * Toàn bộ phần "demo*()" ở dưới là dữ liệu giả, đổi tên view ở return nếu khác.
 * Khi đã có MovieService/ShowtimeService thật, thay các dòng addAttribute()
 * bằng dữ liệu lấy từ Service, rồi xoá các hàm demo*() này đi.
 */
@Controller
@RequestMapping("/booking")
public class BookingController {

    @Autowired
    private MovieService movieService;

    @Autowired
    private ShowDateEngine showDateEngine;

    @Autowired
    private CinemaService cinemaService;

    @Autowired
    private CityService cityService;

    @GetMapping("/movie/{id}")
    public String movieDetail(@PathVariable Integer id,
                              @RequestParam(required = false) Integer cityId,
                              @RequestParam(required = false) String date,
                              Model model) {

        long resolvedCityId = (cityId != null) ? cityId : -1L;
        String resolvedDate =  date != null ? date : LocalDate.now().toString();

        Movie movie = movieService.getMovieById(id);

        LocalDate selectDate = date !=null ? LocalDate.parse(date) : null;

        List<City> cities = cityService.getAllCities();

        List<CinemaScheduleResponse> cinemas = cinemaService.getCinemaByCityAndDateAndMovie(cityId,selectDate,id);

        model.addAttribute("movie", movie);
        model.addAttribute("showDates",showDateEngine.getShowDate(selectDate));
        model.addAttribute("cities", cities);
        model.addAttribute("selectedCityId", resolvedCityId);
        model.addAttribute("selectedDate", resolvedDate);
        model.addAttribute("cinemaSchedules", cinemas);

        // Đổi đúng theo đường dẫn template thật của bạn, ví dụ "booking/movie-detail"
        return "bookingContext";
    }


}