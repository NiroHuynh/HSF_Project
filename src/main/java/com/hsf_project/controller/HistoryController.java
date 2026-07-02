package com.hsf_project.controller;

import com.hsf_project.entity.Booking;
import com.hsf_project.entity.User;
import com.hsf_project.repository.BookingRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

@Controller
public class HistoryController {

    @Autowired
    private BookingRepository bookingRepository;

    @GetMapping("/ve-cua-toi")
    public String viewBookingHistory(HttpSession session, Model model) {
        // 1. Lấy đúng key "ttdn" của nhóm ra
        User currentUser = (User) session.getAttribute("ttdn");

        if (currentUser == null) {
            return "redirect:/login"; // Hoặc trang nào Bình làm để login
        }

        // 2. Truy vấn dữ liệu theo ID người dùng
        List<Booking> listBookings = bookingRepository.findByUserIdAndIsDeletedFalseOrderByBookingDateDesc(currentUser.getId());
        model.addAttribute("bookings", listBookings);

        // 3. Truyền thêm biến activePage để tô sáng menu "Vé của tôi"
        model.addAttribute("activePage", "ve-cua-toi");

        return "history";
    }
}