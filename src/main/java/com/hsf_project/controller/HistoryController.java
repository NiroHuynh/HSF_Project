package com.hsf_project.controller;

import com.hsf_project.entity.Booking;
import com.hsf_project.entity.User;
import com.hsf_project.repository.BookingRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.math.BigDecimal;

@Controller
@RequestMapping("/customer")
public class HistoryController {

    @Autowired
    private BookingRepository bookingRepository;

    @GetMapping("/history")
    public String viewBookingHistory(Model model, HttpSession session) {
        // AuthInterceptor đã chặn /customer/** khi chưa đăng nhập nên user luôn tồn tại.
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
}
