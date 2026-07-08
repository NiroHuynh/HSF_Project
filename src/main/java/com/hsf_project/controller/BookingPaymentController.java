package com.hsf_project.controller;

import com.hsf_project.entity.Booking;
import com.hsf_project.entity.Combo;
import com.hsf_project.entity.PaymentMethod;
import com.hsf_project.entity.ShowTime;
import com.hsf_project.repository.BookingRepository;
import com.hsf_project.service.ComboService;
import com.hsf_project.service.PaymentMethodService;
import com.hsf_project.service.PromotionService;
import com.hsf_project.service.ShowTimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/booking")
public class BookingPaymentController {

    @Autowired
    private PaymentMethodService paymentMethodService;

    @Autowired
    private PromotionService promotionService;

    @Autowired
    private BookingRepository bookingRepository;

    @GetMapping("/payment")
    public String showPaymentPage(@RequestParam String bookingCode, Model model) {

        // 1. Tìm thông tin Booking từ DB dựa vào mã code
        Booking booking = bookingRepository.findByBookingCodeAndIsDeletedFalse(bookingCode)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy mã đơn hàng: " + bookingCode));

        // 2. Tính toán số giây đếm ngược còn lại (Đồng bộ tổng 15 phút)
        long secondsLeft = Duration.between(LocalDateTime.now(), booking.getExpiredAt()).toSeconds();

        // Nếu quá giờ giữ ghế, lập tức đá user về trang phim
        if (secondsLeft <= 0) {
            return "redirect:/home";
        }

        // 3. Lấy danh sách các ghế đã chọn từ Booking
        List<String> selectedSeats = booking.getTickets().stream()
                .map(ticket -> ticket.getSeat().getSeatCode())
                .toList();

        // 4. Lấy danh sách Combo khách hàng đã chọn (Đã được lưu trong bảng trung gian booking_combo ở bước trước)
        // Ánh xạ sang cấu trúc SelectedCombo để Thymeleaf render ra giao diện hiển thị
        List<SelectedCombo> selectedCombos = booking.getBookingCombos().stream()
                .map(bc -> new SelectedCombo(
                        bc.getCombo().getName(),
                        bc.getQuantity(),
                        bc.getTotalPrice()
                )).toList();

        // 5. Lấy thông tin Suất chiếu từ một chiếc vé bất kỳ trong Booking này
        ShowTime showtime = booking.getTickets().get(0).getShowtime();

        // 6. Lấy danh sách các phương thức thanh toán đang kích hoạt (VNPay, MoMo,...)
        List<PaymentMethod> paymentMethods = paymentMethodService.getActiveMethods();
        Long defaultPaymentMethodId = paymentMethods.isEmpty() ? null : paymentMethods.get(0).getId();

        // 7. Đẩy toàn bộ dữ liệu sạch từ DB ra Model cho giao diện
        model.addAttribute("bookingCode", bookingCode);
        model.addAttribute("secondsLeft", secondsLeft); //Số giây còn lại cho đồng hồ đếm ngược
        model.addAttribute("selectedSeats", selectedSeats);
        model.addAttribute("selectedCombos", selectedCombos);

        // Tiền nong lấy trực tiếp từ DB chốt ở Backend, cực kỳ an toàn
        model.addAttribute("totalAmount", booking.getTotalAmount());     // Tổng tiền trước giảm giá
        model.addAttribute("discountAmount", booking.getDiscountAmount()); // Tiền được giảm ban đầu (0)
        model.addAttribute("finalAmount", booking.getFinalAmount());       // Số tiền cuối cùng phải trả

        model.addAttribute("showtimeInfo", loadShowtimeInfo(showtime));
        model.addAttribute("paymentMethods", paymentMethods);
        model.addAttribute("defaultPaymentMethodId", defaultPaymentMethodId);

        return "bookingPayment"; // Trả về template bookingPayment.html của em
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

    // Tinh chỉnh hàm helper nhận thẳng thực thể ShowTime đã lấy được ở trên, đỡ phải query DB lại một lần nữa
    private ShowtimeInfo loadShowtimeInfo(ShowTime showtime) {
        String startTimeLabel = showtime.getStartTime()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        String roomName = showtime.getRoom().getName();
        String formatLabel = "CINEMAX " + showtime.getRoom().getRoomType();

        return new ShowtimeInfo(
                showtime.getMovie().getTitle(),
                showtime.getMovie().getPosterUrl(),
                startTimeLabel,
                roomName,
                formatLabel
        );
    }

    public record SelectedCombo(String name, int quantity, BigDecimal lineTotal) {
    }

    public record ShowtimeInfo(String movieTitle, String posterUrl, String startTime, String roomName, String formatLabel) {
    }
}