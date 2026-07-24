package com.hsf_project.controller;

import com.hsf_project.entity.Booking;
import com.hsf_project.entity.BookingCombo;
import com.hsf_project.entity.Combo;
import com.hsf_project.repository.BookingComboRepository;
import com.hsf_project.repository.BookingRepository;
import com.hsf_project.service.BookingComboService;
import com.hsf_project.service.ComboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/booking")
public class BookingComboController {

    @Autowired
    private ComboService comboService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private BookingComboRepository bookingComboRepository;

    @Autowired
    private BookingComboService bookingComboService;

    @GetMapping("/combo")
    public String showComboPage(@RequestParam String bookingCode, Model model) {

        // 1. Tìm thông tin Booking tạm thời từ DB dựa vào mã code
        Booking booking = bookingRepository.findByBookingCodeAndIsDeletedFalse(bookingCode)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy mã đơn hàng: " + bookingCode));

        // 2. Tính toán số giây đếm ngược còn lại (Khóa 15 phút tổng);
        // booking cũ không có expiredAt thì coi như đã hết hạn
        long secondsLeft = booking.getExpiredAt() == null ? 0
                : Duration.between(LocalDateTime.now(), booking.getExpiredAt()).toSeconds();

        // Nếu lỡ quá giờ giữ ghế trước khi kịp load trang, lập tức đá user về trang chọn phim/suất chiếu
        if (secondsLeft <= 0) {
            return "redirect:/home";
        }

        // 3. Lấy danh sách các ghế đã chọn (được lưu thông qua danh sách các Ticket của Booking này)
        List<String> selectedSeats = booking.getTickets().stream()
                .map(ticket -> ticket.getSeat().getSeatCode())
                .toList();

        // 4. Lấy danh sách các Combo bắp nước đang hoạt động để hiển thị lên giao diện
        List<Combo> combos = comboService.getActiveCombos();

        // 5. Đẩy toàn bộ dữ liệu ra Model để Thymeleaf render giao diện
        model.addAttribute("bookingCode", bookingCode);
        model.addAttribute("secondsLeft", secondsLeft); //Đẩy số giây đếm ngược cho JavaScript
        model.addAttribute("selectedSeats", selectedSeats); // Để hiển thị cho khách xem lại họ đã chọn ghế nào
        model.addAttribute("totalAmount", booking.getTotalAmount()); // Tổng tiền ghế hiện tại
        model.addAttribute("combos", combos);

        return "bookingCombo"; // Trả về file bookingCombo.html của em
    }

    @PostMapping("/combo/save")
    public String saveBookingCombos(
            @RequestParam String bookingCode,
            @RequestParam Map<String, String> allParams
    ) {
        try {
            bookingComboService.saveBookingCombos(bookingCode, allParams);
            return "redirect:/booking/payment?bookingCode=" + bookingCode;
        } catch (IllegalStateException e) {
            // Bắt lỗi hết hạn từ Service bắn ra
            return "redirect:/movies/error?error=timeout";
        }
    }


}