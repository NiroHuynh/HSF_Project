package com.hsf_project.controller;

import com.hsf_project.entity.Cinema;
import com.hsf_project.entity.City;
import com.hsf_project.entity.ShowTime;
import com.hsf_project.repository.ShowTimeRepository;
import com.hsf_project.service.CinemaService;
import com.hsf_project.service.CityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
public class CinemaPageController {

    @Autowired
    private CityService cityService;

    @Autowired
    private CinemaService cinemaService;

    @Autowired
    private ShowTimeRepository showTimeRepository;

    @GetMapping("/rap")
    public String rapPage(@RequestParam(required = false) Integer cityId,
                          @RequestParam(required = false) Integer cinemaId,
                          @RequestParam(required = false) String date,
                          Model model) {

        List<City> cities = cityService.getAllCities();

        if (cityId == null && !cities.isEmpty()) {
            cityId = cities.get(0).getId();
        }

        City selectedCity = cityId != null ? cityService.getCityById(cityId) : null;
        if (selectedCity == null && !cities.isEmpty()) {
            selectedCity = cities.get(0);
            cityId = selectedCity.getId();
        }

        List<Cinema> cinemas = cityId != null
                ? cinemaService.getCinemaWithRoomsByCityId(cityId)
                : Collections.emptyList();

        LocalDate selectedDate = date != null ? LocalDate.parse(date) : LocalDate.now();

        List<LocalDate> dateStrip = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 10; i++) {
            dateStrip.add(today.plusDays(i));
        }

        Map<Integer, List<String>> formatMap = new LinkedHashMap<>();
        for (Cinema c : cinemas) {
            Set<String> formats = new LinkedHashSet<>();
            if (c.getCinemaRooms() != null) {
                c.getCinemaRooms().forEach(r -> {
                    if (r.getRoomType() != null && (r.getIsDeleted() == null || !r.getIsDeleted())) {
                        formats.add(r.getRoomType());
                    }
                });
            }
            formatMap.put(c.getId(), new ArrayList<>(formats));
        }

        Integer openCinemaId = cinemaId;

        Map<Integer, Map<String, List<ShowTimeSlot>>> showtimeMap = new LinkedHashMap<>();
        if (openCinemaId != null) {
            List<ShowTime> showTimes = showTimeRepository.findByCinemaAndDate(
                    openCinemaId, selectedDate.atStartOfDay(), selectedDate.plusDays(1).atStartOfDay());
            Map<String, List<ShowTimeSlot>> grouped = new LinkedHashMap<>();
            for (ShowTime st : showTimes) {
                String key = st.getMovie().getId() + "|" + st.getRoom().getRoomType();
                grouped.computeIfAbsent(key, k -> new ArrayList<>())
                        .add(new ShowTimeSlot(st));
            }
            showtimeMap.put(openCinemaId, grouped);
        }

        model.addAttribute("cities", cities);
        model.addAttribute("selectedCityId", cityId);
        model.addAttribute("selectedCity", selectedCity);
        model.addAttribute("cinemas", cinemas);
        model.addAttribute("selectedDate", selectedDate);
        model.addAttribute("dateStrip", dateStrip);
        model.addAttribute("formatMap", formatMap);
        model.addAttribute("openCinemaId", openCinemaId);
        model.addAttribute("showtimeMap", showtimeMap);
        model.addAttribute("activePage", "rap");

        return "rap";
    }

    public static class ShowTimeSlot {
        private final Long id;
        private final String timeLabel;
        private final ShowTime showTime;

        public ShowTimeSlot(ShowTime st) {
            this.id = st.getId();
            this.timeLabel = st.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm"));
            this.showTime = st;
        }

        public Long getId() { return id; }
        public String getTimeLabel() { return timeLabel; }
        public com.hsf_project.entity.Movie getMovie() { return showTime.getMovie(); }
    }
}
