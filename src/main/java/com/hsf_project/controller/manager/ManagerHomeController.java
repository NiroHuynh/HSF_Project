package com.hsf_project.controller.manager;

import com.hsf_project.entity.User;
import com.hsf_project.service.ManagerDashboardService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Controller
@RequestMapping("/manager")
public class ManagerHomeController {

    @Autowired
    private ManagerDashboardService managerDashboardService;

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("ttdn");

        if (user != null) {
            model.addAttribute("managerName",
                    user.getLastName() + " " + user.getFirstName());
            model.addAttribute("managerEmail", user.getEmail());
            model.addAttribute("managerCinemaName",
                    user.getCinema() != null ? user.getCinema().getName() : "");
        }

        model.addAttribute("activePage", "dashboard");

        int year = LocalDate.now().getYear();
        LocalDateTime from = LocalDateTime.of(year, 1,  1,  0,  0,  0);
        LocalDateTime to   = LocalDateTime.of(year, 12, 31, 23, 59, 59);

        if (user != null && user.getCinema() != null) {
            Map<String, Object> stats = managerDashboardService.getStats(
                    user.getCinema().getId(), from, to, "year");
            model.addAttribute("revenue",     stats.get("revenue"));
            model.addAttribute("showtimes",   stats.get("showtimes"));
            model.addAttribute("tickets",     stats.get("tickets"));
            model.addAttribute("customers",   stats.get("customers"));
            model.addAttribute("chartLabels", stats.get("chartLabels"));
            model.addAttribute("chartData",   stats.get("chartData"));
        } else {
            model.addAttribute("revenue",   "0 đ");
            model.addAttribute("showtimes", 0L);
            model.addAttribute("tickets",   0L);
            model.addAttribute("customers", 0L);
            model.addAttribute("chartLabels",
                    java.util.List.of("T1","T2","T3","T4","T5","T6","T7","T8","T9","T10","T11","T12"));
            model.addAttribute("chartData", new long[12]);
        }

        return "manager/home";
    }

    @GetMapping("/dashboard/stats")
    @ResponseBody
    public Map<String, Object> getStats(
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

        return managerDashboardService.getStats(user.getCinema().getId(), fromDT, toDT, mode);
    }

    @GetMapping("/dashboard/search")
    @ResponseBody
    public Map<String, Object> searchBooking(
            @RequestParam String code,
            HttpSession session) {

        User user = (User) session.getAttribute("ttdn");
        if (user == null || user.getCinema() == null) {
            return Map.of("found", false);
        }

        return managerDashboardService.searchBooking(code, user.getCinema().getId());
    }

    /**
     * POST /manager/dashboard/export?code=CMX...
     * Đổi booking status CONFIRMED → EXPORTED
     */
    @PostMapping("/dashboard/export")
    @ResponseBody
    public Map<String, Object> exportBooking(
            @RequestParam String code,
            HttpSession session) {

        User user = (User) session.getAttribute("ttdn");
        if (user == null || user.getCinema() == null) {
            return Map.of("success", false, "message", "Chưa đăng nhập.");
        }

        return managerDashboardService.exportBooking(code, user.getCinema().getId());
    }
}