package com.hsf_project.controller;

import com.hsf_project.dto.response.PaymentPageData;
import com.hsf_project.entity.*;
import com.hsf_project.entity.enums.BookingStatus;
import com.hsf_project.repository.BookingRepository;
import com.hsf_project.repository.TicketRepository;
import com.hsf_project.service.BookingPaymentService;
import com.hsf_project.service.PaymentMethodService;
import com.hsf_project.service.PromotionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
@RequestMapping("/booking")
public class BookingPaymentController {

    @Autowired
    private PaymentMethodService paymentMethodService;

    @Autowired
    private PromotionService promotionService;

    @Autowired
    private BookingPaymentService bookingPaymentService;

    @GetMapping("/payment")
    public String showPaymentPage(@RequestParam String bookingCode, Model model) {

        try {
            // 1. Gọi Service lấy dữ liệu đã gom trong DTO
            PaymentPageData data = bookingPaymentService.getPaymentPageData(bookingCode);

            // 2. Đẩy thông tin chung ra Model
            model.addAttribute("bookingCode",            data.getBookingCode());
            model.addAttribute("secondsLeft",            data.getSecondsLeft());
            model.addAttribute("selectedSeats",          data.getSelectedSeats());
            model.addAttribute("selectedCombos",         data.getSelectedCombos());

            // 3. Đẩy thông tin tiền tệ & PTTT
            model.addAttribute("totalAmount",            data.getTotalAmount());
            model.addAttribute("discountAmount",         data.getDiscountAmount());
            model.addAttribute("finalAmount",            data.getFinalAmount());

            model.addAttribute("paymentMethods",         data.getPaymentMethods());
            model.addAttribute("defaultPaymentMethodId", data.getPaymentMethods().isEmpty() ? null : data.getPaymentMethods().get(0).getId());

            // 4. Đẩy thông tin phim/rạp từ Showtime
            var showtime = data.getShowtime();
            model.addAttribute("movieTitle",    showtime.getMovie().getTitle());
            model.addAttribute("moviePosterUrl", showtime.getMovie().getPosterUrl());
            model.addAttribute("showTimeDate",  showtime.getStartTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            model.addAttribute("showTimeTime",  showtime.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")));
            model.addAttribute("roomName",      showtime.getRoom().getName());
            model.addAttribute("cinemaName",    showtime.getRoom().getCinema().getName());

            return "bookingPayment";

        } catch (IllegalStateException e) {
            // Xử lý khi lỡ hết thời gian 15 phút giữ ghế
            return "redirect:/movies/error?error=timeout";
        }
    }

    /**
     * Giữ nguyên endpoint AJAX cho tính năng áp dụng mã giảm giá của em
     */
    @PostMapping("/apply-promo")
    @ResponseBody
    public PromotionService.PromotionResult applyPromo(
            @RequestParam String code,
            @RequestParam BigDecimal orderAmount) {
        return promotionService.validate(code, orderAmount);
    }

//    // Tinh chỉnh hàm helper nhận thẳng thực thể ShowTime đã lấy được ở trên, đỡ phải query DB lại một lần nữa
//    private ShowtimeInfo loadShowtimeInfo(ShowTime showtime) {
//        String startTimeLabel = showtime.getStartTime()
//                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
//        String roomName = showtime.getRoom().getName();
//        String formatLabel = "CINEMAX " + showtime.getRoom().getRoomType();
//
//        return new ShowtimeInfo(
//                showtime.getMovie().getTitle(),
//                showtime.getMovie().getPosterUrl(),
//                startTimeLabel,
//                roomName,
//                formatLabel
//        );
//    }

    public record SelectedCombo(String name, int quantity, BigDecimal lineTotal) {
    }

    public record ShowtimeInfo(String movieTitle, String posterUrl, String startTime, String roomName, String formatLabel) {
    }

    @PostMapping("/cancel")
    public String cancelBooking(@RequestParam String bookingCode) {
        bookingPaymentService.cancelBooking(bookingCode);
        return "redirect:/movies";
    }
}