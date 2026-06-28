package com.hsf_project.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

    @GetMapping("/movie/{id}")
    public String movieDetail(@PathVariable Long id,
                              @RequestParam(required = false) Long cityId,
                              @RequestParam(required = false) String date,
                              Model model) {

        long resolvedCityId = (cityId != null) ? cityId : 1L;
        String resolvedDate = (date != null) ? date : "2026-10-20";

        model.addAttribute("movie", demoMovie());
        model.addAttribute("showDates", demoShowDates(resolvedDate));
        model.addAttribute("cities", demoCities());
        model.addAttribute("selectedCityId", resolvedCityId);
        model.addAttribute("selectedDate", resolvedDate);
        model.addAttribute("cinemaSchedules", demoCinemaSchedules());

        // Đổi đúng theo đường dẫn template thật của bạn, ví dụ "booking/movie-detail"
        return "bookingContext";
    }

    // ===================== DEMO DATA (xoá khi có Service thật) =====================

    private MovieVM demoMovie() {
        return new MovieVM(
                1L,
                "JOKER: FOLIE À DEUX",
                "https://via.placeholder.com/360x540",
                138,
                8.9,
                "T16",
                List.of(new GenreVM("Action"), new GenreVM("Sci-Fi"))
        );
    }

    private List<ShowDateVM> demoShowDates(String selectedIsoDate) {
        String[] months = {"10", "10", "10", "10", "10"};
        String[] days = {"20", "21", "22", "23", "24"};
        String[] weekdays = {"WED", "THU", "FRI", "SAT", "SUN"};
        List<ShowDateVM> result = new java.util.ArrayList<>();
        for (int i = 0; i < days.length; i++) {
            String iso = "2026-" + months[i] + "-" + days[i];
            boolean selected = iso.equals(selectedIsoDate) || (i == 0 && selectedIsoDate == null);
            result.add(new ShowDateVM(iso, Integer.parseInt(days[i]), months[i], weekdays[i], selected));
        }
        return result;
    }

    private List<CityVM> demoCities() {
        return List.of(
                new CityVM(1L, "Hồ Chí Minh"),
                new CityVM(2L, "Hà Nội"),
                new CityVM(3L, "Đà Nẵng"),
                new CityVM(4L, "Cần Thơ")
        );
    }

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

    public static class GenreVM {
        private final String name;
        public GenreVM(String name) { this.name = name; }
        public String getName() { return name; }
    }

    public static class MovieVM {
        private final Long id;
        private final String title;
        private final String posterUrl;
        private final Integer durationMinutes;
        private final Double averageRating;
        private final String ageRating;
        private final List<GenreVM> genres;

        public MovieVM(Long id, String title, String posterUrl, Integer durationMinutes,
                       Double averageRating, String ageRating, List<GenreVM> genres) {
            this.id = id;
            this.title = title;
            this.posterUrl = posterUrl;
            this.durationMinutes = durationMinutes;
            this.averageRating = averageRating;
            this.ageRating = ageRating;
            this.genres = genres;
        }

        public Long getId() { return id; }
        public String getTitle() { return title; }
        public String getPosterUrl() { return posterUrl; }
        public Integer getDurationMinutes() { return durationMinutes; }
        public Double getAverageRating() { return averageRating; }
        public String getAgeRating() { return ageRating; }
        public List<GenreVM> getGenres() { return genres; }
    }

    public static class ShowDateVM {
        private final String isoDate;
        private final int dayOfMonth;
        private final String month;
        private final String weekdayLabel;
        private final boolean selected;

        public ShowDateVM(String isoDate, int dayOfMonth, String month, String weekdayLabel, boolean selected) {
            this.isoDate = isoDate;
            this.dayOfMonth = dayOfMonth;
            this.month = month;
            this.weekdayLabel = weekdayLabel;
            this.selected = selected;
        }

        public String getIsoDate() { return isoDate; }
        public int getDayOfMonth() { return dayOfMonth; }
        public String getMonth() { return month; }
        public String getWeekdayLabel() { return weekdayLabel; }
        public boolean isSelected() { return selected; }
    }

    public static class CityVM {
        private final Long id;
        private final String name;
        public CityVM(Long id, String name) { this.id = id; this.name = name; }
        public Long getId() { return id; }
        public String getName() { return name; }
    }

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