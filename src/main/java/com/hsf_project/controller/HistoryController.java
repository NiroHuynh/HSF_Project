package com.hsf_project.controller;

import com.hsf_project.entity.Booking;
import com.hsf_project.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/customer")
public class HistoryController {

    @Autowired
    private BookingRepository bookingRepository;

    @GetMapping("/history")
    public String viewBookingHistory(Model model) {
        // Giả lập userId hiện tại là 1L.
        // Khi nhóm bạn tích hợp Spring Security, đoạn này sẽ thay bằng logic lấy từ Principal/UserDetails
        Long currentUserId = 1L;

        List<Booking> listBookings = bookingRepository.findByUserIdAndIsDeletedFalseOrderByBookingDateDesc(currentUserId);

        // Đẩy danh sách sang giao diện Thymeleaf
        model.addAttribute("bookings", listBookings);

        // Trả về file HTML nằm trong thư mục: src/main/resources/templates/customer/history.html
        return "customer/history";
    }
}