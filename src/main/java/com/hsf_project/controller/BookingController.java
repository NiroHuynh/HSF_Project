package com.hsf_project.controller;

import com.hsf_project.dto.response.CinemaScheduleResponse;
import com.hsf_project.engine.ShowDateEngine;
import com.hsf_project.entity.Cinema;
import com.hsf_project.entity.City;
import com.hsf_project.entity.Movie;
import com.hsf_project.entity.User;
import jakarta.servlet.http.HttpSession;
import com.hsf_project.service.BookingConfirmService;
import com.hsf_project.service.CinemaService;
import com.hsf_project.service.CityService;
import com.hsf_project.service.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
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

    @Autowired
    private BookingConfirmService bookingConfirmService;

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

    @PostMapping("/initiate")
    public String initiateBooking(
            @RequestParam Long showtimeId,
            @RequestParam String seatIds, // Nhận chuỗi dạng "A1,A2" từ Form
            HttpSession session
    ) {
        // AuthInterceptor đã chặn /booking/** khi chưa đăng nhập nên user luôn tồn tại.
        User currentUser = (User) session.getAttribute("ttdn");

        // 1. Chuyển chuỗi "A1,A2" thành List<String>
        List<String> listSeats = Arrays.stream(seatIds.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        // 2. Tạo booking PENDING giữ ghế (chưa chọn combo/voucher nên truyền null/ZERO)
        var result = bookingConfirmService.confirmBooking(
                currentUser.getId(),
                showtimeId,
                listSeats,
                null,                    // comboQuantities
                null,                    // paymentMethodId
                null,                    // promotionId
                BigDecimal.ZERO
        );

        // 3. Điều hướng sang trang combo kèm theo mã bookingCode vừa sinh ra
        return "redirect:/booking/combo?bookingCode=" + result.bookingCode();
    }


}