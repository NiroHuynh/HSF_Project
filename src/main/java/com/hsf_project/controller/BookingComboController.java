package com.hsf_project.controller;

import com.hsf_project.entity.Booking;
import com.hsf_project.entity.BookingCombo;
import com.hsf_project.entity.Combo;
import com.hsf_project.repository.BookingComboRepository;
import com.hsf_project.repository.BookingRepository;
import com.hsf_project.service.ComboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
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

//    @Autowired
//    private ComboService bookingComboService;
//    @Autowired
//    private BookingRepository bookingRepository;
//
//    @GetMapping("/combo")
//    public String showComboPage(
//            @RequestParam Long showtimeId,
//            @RequestParam String seatIds,
//            @RequestParam(defaultValue = "0") BigDecimal seatTotal,
//            Model model) {
//
//        List<String> selectedSeats = Arrays.stream(seatIds.split(","))
//                .map(String::trim)
//                .filter(s -> !s.isEmpty())
//                .toList();
//
//        List<Combo> combos = bookingComboService.getActiveCombos();
//
//        model.addAttribute("showtimeId", showtimeId);
//        model.addAttribute("seatIds", seatIds);
//        model.addAttribute("selectedSeats", selectedSeats);
//        model.addAttribute("seatTotal", seatTotal);
//        model.addAttribute("combos", combos);
//
//        return "bookingCombo";
//    }

    @Autowired
    private ComboService comboService; // Đổi tên biến cho đúng ngữ nghĩa dịch vụ Combo

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private BookingComboRepository bookingComboRepository;

    @GetMapping("/combo")
    public String showComboPage(@RequestParam String bookingCode, Model model) {

        // 1. Tìm thông tin Booking tạm thời từ DB dựa vào mã code
        Booking booking = bookingRepository.findByBookingCodeAndIsDeletedFalse(bookingCode)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy mã đơn hàng: " + bookingCode));

        // 2. Tính toán số giây đếm ngược còn lại (Khóa 15 phút tổng)
        long secondsLeft = Duration.between(LocalDateTime.now(), booking.getExpiredAt()).toSeconds();

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
        model.addAttribute("secondsLeft", secondsLeft); // 🌟 Đẩy số giây đếm ngược cho JavaScript
        model.addAttribute("selectedSeats", selectedSeats); // Để hiển thị cho khách xem lại họ đã chọn ghế nào
        model.addAttribute("totalAmount", booking.getTotalAmount()); // Tổng tiền ghế hiện tại
        model.addAttribute("combos", combos);

        return "bookingCombo"; // Trả về file bookingCombo.html của em
    }

    @PostMapping("/combo/save")
    public String saveBookingCombos(
            @RequestParam String bookingCode,
            @RequestParam Map<String, String> allParams // Hứng toàn bộ dữ liệu combo_* từ form gửi lên
    ) {
        // 1. Lấy Booking từ DB ra
        Booking booking = bookingRepository.findByBookingCodeAndIsDeletedFalse(bookingCode)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng: " + bookingCode));

        // Kiểm tra nếu đơn hàng đã hết hạn giữ ghế thì không cho thao tác tiếp
        if (java.time.LocalDateTime.now().isAfter(booking.getExpiredAt())) {
            return "redirect:/movies/error?error=timeout";
        }

        // 2. Duyệt dữ liệu tham số để lọc ra các combo khách chọn
        Map<Long, Integer> comboQuantities = new java.util.LinkedHashMap<>();
        BigDecimal comboTotal = BigDecimal.ZERO;

        for (Map.Entry<String, String> entry : allParams.entrySet()) {
            if (!entry.getKey().startsWith("combo_")) {
                continue;
            }
            Long comboId = Long.valueOf(entry.getKey().substring("combo_".length()));
            int qty = Integer.parseInt(entry.getValue());

            if (qty > 0) {
                comboQuantities.put(comboId, qty);
                Combo combo = comboService.getById(comboId);
                comboTotal = comboTotal.add(combo.getPrice().multiply(BigDecimal.valueOf(qty)));
            }
        }

        // 3. Xóa các combo cũ đã lưu trước đó của Booking này (nếu có - đề phòng trường hợp user quay lại sửa)
        // Giả sử em dùng bookingComboRepository để dọn dẹp theo booking.getId()
        // bookingComboRepository.deleteByBookingId(booking.getId());

        // 4. Lưu danh sách combo mới vào bảng trung gian booking_combo
        for (Map.Entry<Long, Integer> entry : comboQuantities.entrySet()) {
            Combo combo = comboService.getById(entry.getKey());
            BookingCombo line = new BookingCombo();
            line.setBooking(booking);
            line.setCombo(combo);
            line.setQuantity(entry.getValue());
            line.setUnitPrice(combo.getPrice());
            line.setTotalPrice(combo.getPrice().multiply(BigDecimal.valueOf(entry.getValue())));
            line.setCreatedAt(java.time.LocalDateTime.now());
            bookingComboRepository.save(line); // Tiêm Repository tương ứng vào nhé
        }

        // 5. Tính toán lại tổng tiền vé (đã lưu sẵn) + tổng tiền combo vừa chọn
        // Giả sử tiền vé ban đầu được tính lại từ danh sách Ticket gắn với Booking này
        BigDecimal seatTotal = booking.getTickets().stream()
                .map(t -> t.getTicketPrice().getPrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalAmount = seatTotal.add(comboTotal);
        BigDecimal finalAmount = totalAmount.subtract(booking.getDiscountAmount());

        // ============================================================
        // BƯỚC 6: CẬP NHẬT HOÁ ĐƠN VÀ KHÓA CHẶT THỜI GIAN GỐC
        // ============================================================

        // Đọc lại thời gian hết hạn gốc trước khi save để chắc chắn không bị Hibernate làm mất dữ liệu
        LocalDateTime originalExpiredAt = booking.getExpiredAt();

        booking.setTotalAmount(totalAmount);
        booking.setFinalAmount(finalAmount);
        booking.setUpdatedAt(java.time.LocalDateTime.now());

        // Ép buộc đối tượng booking sử dụng lại mốc expiredAt cũ từ bước chọn ghế
        if (originalExpiredAt != null) {
            booking.setExpiredAt(originalExpiredAt);
        }

        bookingRepository.save(booking);

        // 7. CHUYỂN TRANG: Điều hướng sang trang thanh toán gọn gàng bằng đúng mã đơn
        return "redirect:/booking/payment?bookingCode=" + bookingCode;
    }

}