package com.se196222.mvc.choosecinema.controller;

import com.se196222.mvc.choosecinema.entity.Cinema;
import com.se196222.mvc.choosecinema.entity.City;
import com.se196222.mvc.choosecinema.entity.ShowTime;
import com.se196222.mvc.choosecinema.service.CinemaService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.*;

public class CinemaController {
    private final CinemaService cinemaService;

    public CinemaController(CinemaService cinemaService) {
        this.cinemaService = cinemaService;
    }

    // ── GET /rap ─────────────────────────────────────────────
    // Main cinema page:
    //   cityId   - selected city (default: first city)
    //   cinemaId - expanded cinema card (optional)
    //   date     - selected date for showtimes (default: today)
    @GetMapping("/rap")
    public String showCinemaPage(
            @RequestParam(value = "cityId",   required = false) Integer cityId,
            @RequestParam(value = "cinemaId", required = false) Integer cinemaId,
            @RequestParam(value = "date",     required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)      LocalDate date,
            Model model) {

        // 1. Load all cities
        List<City> cities = cinemaService.getAllCities();
        if (cities.isEmpty()) {
            model.addAttribute("cities", cities);
            model.addAttribute("cinemas", Collections.emptyList());
            return "cinema/rap";
        }

        // 2. Default to first city if not selected
        if (cityId == null) cityId = cities.get(0).getId();
        final Integer selectedCityId = cityId;

        // 3. Default date = today
        if (date == null) date = LocalDate.now();

        // 4. Load cinemas for selected city
        List<Cinema> cinemas = cinemaService.getCinemasByCity(selectedCityId);

        // 5. Build format-chip map  cinemaId → ["2D","IMAX", ...]
        Map<Integer, List<String>> formatMap = new LinkedHashMap<>();
        for (Cinema c : cinemas) {
            formatMap.put(c.getId(),
                    cinemaService.getFormatsByCinema(c.getId()));
        }

        // 6. If a cinema is expanded, load its showtime data
        Map<Integer, Map<String, List<ShowTime>>> showtimeMap = new HashMap<>();
        if (cinemaId != null) {
            List<ShowTime> slots = cinemaService.getShowtimes(cinemaId, date);

            // Group: movieId → (format → sorted showtimes)
            Map<Integer, Map<String, List<ShowTime>>> grouped = new LinkedHashMap<>();
            for (ShowTime st : slots) {
                int mid   = st.getMovie().getId();
                String fmt = st.getRoom().getDisplayFormat();
                grouped
                        .computeIfAbsent(mid, k -> new LinkedHashMap<>())
                        .computeIfAbsent(fmt, k -> new ArrayList<>())
                        .add(st);
            }
            showtimeMap.put(cinemaId, new LinkedHashMap<>());
            // Flatten for template: cinemaId → { "movieId|format" → [ShowTime,...] }
            // (Simpler to iterate in Thymeleaf as a flat structure)
            grouped.forEach((mid, fmtMap) ->
                    fmtMap.forEach((fmt, list) ->
                            showtimeMap.get(cinemaId)
                                    .put(mid + "|" + fmt, list)));
        }

        // 7. Build 14-day date strip for the scroller
        List<LocalDate> dateStrip = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 14; i++) dateStrip.add(today.plusDays(i));

        // 8. Find active city name
        City selectedCity = cities.stream()
                .filter(c -> c.getId().equals(selectedCityId))
                .findFirst().orElse(cities.get(0));

        // ── Push to model ─────────────────────────────────────
        model.addAttribute("cities",         cities);
        model.addAttribute("selectedCityId", selectedCityId);
        model.addAttribute("selectedCity",   selectedCity);
        model.addAttribute("cinemas",        cinemas);
        model.addAttribute("formatMap",      formatMap);
        model.addAttribute("openCinemaId",   cinemaId);
        model.addAttribute("selectedDate",   date);
        model.addAttribute("dateStrip",      dateStrip);
        model.addAttribute("showtimeMap",    showtimeMap);

        return "cinema/rap";
    }
}
