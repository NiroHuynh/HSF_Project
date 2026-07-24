package com.hsf_project.controller.manager;

import com.hsf_project.entity.User;
import com.hsf_project.service.ManagerRevenueService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Controller
@RequestMapping("/manager")
public class ManagerRevenueController {

    @Autowired
    private ManagerRevenueService managerRevenueService;

    @GetMapping("/revenue")
    public String revenue(HttpSession session, Model model) {
        User user = (User) session.getAttribute("ttdn");

        if (user != null) {
            model.addAttribute("managerName",
                    user.getLastName() + " " + user.getFirstName());
            model.addAttribute("managerEmail", user.getEmail());
            model.addAttribute("managerCinemaName",
                    user.getCinema() != null ? user.getCinema().getName() : "");
        }

        model.addAttribute("activePage", "revenue");

        // Mặc định năm hiện tại, mode = year
        int year = LocalDate.now().getYear();
        LocalDateTime from = LocalDateTime.of(year, 1,  1,  0,  0,  0);
        LocalDateTime to   = LocalDateTime.of(year, 12, 31, 23, 59, 59);

        if (user != null && user.getCinema() != null) {
            Map<String, Object> stats =
                    managerRevenueService.getRevenueStats(user.getCinema().getId(), from, to, "year");
            stats.forEach(model::addAttribute);
        } else {
            model.addAttribute("totalRevenue",    "0 đ");
            model.addAttribute("ticketRevenue",   "0 đ");
            model.addAttribute("comboRevenue",    "0 đ");
            model.addAttribute("ticketCount",     "0");
            model.addAttribute("comboCount",      "0");
            model.addAttribute("ticketPercent",   "0%");
            model.addAttribute("comboPercent",    "0%");
            model.addAttribute("topMovies",       java.util.List.of());
        }

        return "manager/revenue";
    }

    /** AJAX — cập nhật toàn bộ trang khi đổi date range / mode */
    @GetMapping("/revenue/stats")
    @ResponseBody
    public Map<String, Object> getRevenueStats(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam(defaultValue = "year") String mode,
            HttpSession session) {

        User user = (User) session.getAttribute("ttdn");
        if (user == null || user.getCinema() == null) {
            return Map.of("error", "Chưa đăng nhập hoặc không có chi nhánh.");
        }

        LocalDateTime fromDT = LocalDate.parse(from).atStartOfDay();
        LocalDateTime toDT   = LocalDate.parse(to).atTime(23, 59, 59);

        return managerRevenueService.getRevenueStats(user.getCinema().getId(), fromDT, toDT, mode);
    }
}