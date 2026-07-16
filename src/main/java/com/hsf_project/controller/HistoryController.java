package com.hsf_project.controller;

import com.hsf_project.entity.Booking;
import com.hsf_project.entity.User;
import com.hsf_project.entity.BookingStatus;
import com.hsf_project.repository.BookingRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/customer")
public class HistoryController {

    @Autowired
    private BookingRepository bookingRepository;

    @GetMapping("/history")
    public String viewBookingHistory(Model model, HttpSession session) {
        // AuthFilter đã chặn /customer/** khi chưa đăng nhập nên user luôn tồn tại.
        User currentUser = (User) session.getAttribute("ttdn");

        // Truy vấn dữ liệu theo ID người dùng
        List<Booking> listBookings = bookingRepository.findByUserIdAndIsDeletedFalseOrderByBookingDateDesc(currentUser.getId());
        model.addAttribute("bookings", listBookings);
        Map<Long, BigDecimal> ticketSubtotals = new LinkedHashMap<>();
        Map<Long, BigDecimal> comboSubtotals = new LinkedHashMap<>();
        for (Booking booking : listBookings) {
            BigDecimal ticketTotal = booking.getTickets().stream()
                    .map(ticket -> ticket.getDisplayPrice()).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal comboTotal = booking.getBookingCombos().stream()
                    .map(line -> line.getTotalPrice()).reduce(BigDecimal.ZERO, BigDecimal::add);
            ticketSubtotals.put(booking.getId(), ticketTotal);
            comboSubtotals.put(booking.getId(), comboTotal);
        }
        model.addAttribute("ticketSubtotals", ticketSubtotals);
        model.addAttribute("comboSubtotals", comboSubtotals);
        model.addAttribute("activePage", "ve-cua-toi");

        // Trả về file HTML nằm trong thư mục: src/main/resources/templates/customer/history.html
        return "history";
    }

    /** Đánh dấu booking đã được người dùng xuất/in. Chỉ chủ sở hữu vé mới được thực hiện. */
    @PostMapping("/history/{bookingCode}/export")
    @Transactional
    public ResponseEntity<Map<String, String>> exportTicket(@PathVariable String bookingCode, HttpSession session) {
        User currentUser = (User) session.getAttribute("ttdn");
        Booking booking = bookingRepository.findByBookingCodeAndIsDeletedFalse(bookingCode).orElse(null);
        if (booking == null || !booking.getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.notFound().build();
        }
        if (BookingStatus.PENDING.name().equals(booking.getStatus())
                || BookingStatus.CANCELED.name().equals(booking.getStatus())) {
            return ResponseEntity.status(409).body(Map.of("message", "Chỉ vé đã thanh toán mới có thể xuất"));
        }
        booking.setStatus(BookingStatus.EXPORTED.name());
        booking.setUpdatedAt(LocalDateTime.now());
        bookingRepository.save(booking);
        return ResponseEntity.ok(Map.of("status", BookingStatus.EXPORTED.name()));
    }
}
