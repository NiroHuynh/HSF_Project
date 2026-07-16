package com.hsf_project.controller.manager;

import com.hsf_project.dto.response.BookingRowDTO;
import com.hsf_project.entity.User;
import com.hsf_project.service.TicketService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/manager")
public class ManagerTicketController {

    @Autowired
    private TicketService ticketService;

    @GetMapping("/tickets")
    public String listTickets(HttpSession session, Model model) {

        User user = (User) session.getAttribute("ttdn");

        // Sidebar info
        if (user != null) {
            model.addAttribute("managerName",
                    user.getLastName() + " " + user.getFirstName());
            model.addAttribute("managerEmail",
                    user.getEmail());
            model.addAttribute("managerCinemaName",
                    user.getCinema() != null ? user.getCinema().getName() : "");
        }

        // Lấy booking theo cinema của staff đang đăng nhập
        // Nếu user null hoặc không có cinema → trả danh sách rỗng
        List<BookingRowDTO> bookings = Collections.emptyList();

        if (user != null && user.getCinema() != null) {
            bookings = ticketService.getBookingsByCinema(user.getCinema().getId());
        }

        model.addAttribute("bookings",     bookings);
        model.addAttribute("totalCount",   bookings.size());
        model.addAttribute("pendingCount",
                bookings.stream()
                        .filter(b -> "PENDING".equalsIgnoreCase(b.getStatus()))
                        .count());
        model.addAttribute("activePage", "tickets");

        return "manager/ticket";
    }
}