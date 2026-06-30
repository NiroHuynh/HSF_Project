package com.hsf_project.controller;

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
        String resolvedDate = (date != null) ? date : "2026-10-20";

        Movie movie = movieService.getMovieById(id);

        LocalDate selectDate = date !=null ? LocalDate.parse(date) : null;

        List<City> cities = cityService.getAllCities();

        List<Cinema> cinemas = cinemaService.getCinemaByCityAndDate(cityId);

        model.addAttribute("movie", movie);
        model.addAttribute("showDates",showDateEngine.getShowDate(selectDate));
        model.addAttribute("cities", cities);
        model.addAttribute("selectedCityId", resolvedCityId);
        model.addAttribute("selectedDate", resolvedDate);
        model.addAttribute("cinemaSchedules", cinemas);

        // Đổi đúng theo đường dẫn template thật của bạn, ví dụ "booking/movie-detail"
        return "bookingContext";
    }

    // ===================== DEMO DATA (xoá khi có Service thật) =====================

    private List<CinemaScheduleVM> demoCinemaSchedules() {
        Map<String, List<ShowtimeSlotVM>> pandoraGroups = new LinkedHashMap<>();
        pandoraGroups.put("2D", List.of(
                new ShowtimeSlotVM(101L, "10:00"),
                new ShowtimeSlotVM(102L, "13:15"),
                new ShowtimeSlotVM(103L, "16:30")
        ));

        Map<String, List<ShowtimeSlotVM>> libertyGroups = new LinkedHashMap<>();
        libertyGroups.put("3D", List.of(
                new ShowtimeSlotVM(201L, "14:00"),
                new ShowtimeSlotVM(202L, "18:30")
        ));
        libertyGroups.put("IMAX", List.of(
                new ShowtimeSlotVM(203L, "20:30")
        ));

        return List.of(
                new CinemaScheduleVM(1L, "CGV PANDORA CITY",
                        "1/1 Trường Chinh, P. Tây Thạnh, Q. Tân Phú", pandoraGroups),
                new CinemaScheduleVM(2L, "CGV LIBERTY CITYPOINT",
                        "Tầng M - 1, 59-61 Pasteur, Quận 1", libertyGroups)
        );
    }

    // ===================== View Model (mirror của template, đổi sang DTO thật) =====================




    public static class ShowtimeSlotVM {
        private final Long id;
        private final String timeLabel;
        public ShowtimeSlotVM(Long id, String timeLabel) { this.id = id; this.timeLabel = timeLabel; }
        public Long getId() { return id; }
        public String getTimeLabel() { return timeLabel; }
    }

    public static class CinemaScheduleVM {
        private final Long id;
        private final String name;
        private final String address;
        private final Map<String, List<ShowtimeSlotVM>> formatGroups;

        public CinemaScheduleVM(Long id, String name, String address, Map<String, List<ShowtimeSlotVM>> formatGroups) {
            this.id = id;
            this.name = name;
            this.address = address;
            this.formatGroups = formatGroups;
        }

        public Long getId() { return id; }
        public String getName() { return name; }
        public String getAddress() { return address; }
        public Map<String, List<ShowtimeSlotVM>> getFormatGroups() { return formatGroups; }
    }
}