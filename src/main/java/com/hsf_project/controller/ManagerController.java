package com.hsf_project.controller;

import com.hsf_project.dto.dashboard.DashboardStats;
import com.hsf_project.dto.dashboard.MonthlyRevenueDto;
import com.hsf_project.entity.User;
import com.hsf_project.repository.auth.UserRepository;
import com.hsf_project.service.DashboardService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

/**
 * Trang quản lý dành cho MANAGER: doanh thu giới hạn theo rạp mà manager phụ trách.
 * AuthFilter chỉ cho MANAGER (và ADMIN) vào /manager/**.
 */
@Controller
@RequestMapping("/manager")
public class ManagerController {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    @Transactional(readOnly = true)
    public String dashboard(@RequestParam(name = "mode", defaultValue = "month") String mode,
                            @RequestParam(name = "date", required = false) String date,
                            @RequestParam(name = "month", required = false) String month,
                            @RequestParam(name = "year", required = false) Integer year,
                            HttpSession session,
                            Model model) {
        User sessionUser = (User) session.getAttribute("ttdn");
        // User trong session đã detached — nạp lại để truy cập được field cinema (LAZY)
        User user = userRepository.findById(sessionUser.getId()).orElse(null);
        if (user == null || user.getCinema() == null) {
            model.addAttribute("noCinema", true);
            return "manager/dashboard";
        }
        Integer cinemaId = user.getCinema().getId();
        model.addAttribute("cinemaName", user.getCinema().getName());

        LocalDateTime from;
        LocalDateTime to;
        String periodLabel;
        int chartYear;

        switch (mode) {
            case "day" -> {
                LocalDate day = parseDate(date);
                from = day.atStartOfDay();
                to = from.plusDays(1);
                periodLabel = "Ngày " + day.getDayOfMonth() + "/" + day.getMonthValue() + "/" + day.getYear();
                chartYear = day.getYear();
                model.addAttribute("date", day.toString());
            }
            case "year" -> {
                int y = (year != null) ? year : LocalDate.now().getYear();
                from = LocalDate.of(y, 1, 1).atStartOfDay();
                to = from.plusYears(1);
                periodLabel = "Năm " + y;
                chartYear = y;
                model.addAttribute("year", y);
            }
            default -> {
                mode = "month";
                YearMonth ym = parseMonth(month);
                from = ym.atDay(1).atStartOfDay();
                to = from.plusMonths(1);
                periodLabel = "Tháng " + ym.getMonthValue() + "/" + ym.getYear();
                chartYear = ym.getYear();
                model.addAttribute("month", ym.toString());
            }
        }

        DashboardStats stats = dashboardService.getStatsForCinema(from, to, cinemaId);
        model.addAttribute("stats", stats);
        model.addAttribute("monthlyData", dashboardService.getMonthlyRevenue(chartYear, cinemaId)
                .stream().map(MonthlyRevenueDto::revenue).toList());
        model.addAttribute("chartYear", chartYear);
        model.addAttribute("mode", mode);
        model.addAttribute("periodLabel", periodLabel);
        return "manager/dashboard";
    }

    private LocalDate parseDate(String raw) {
        try {
            return LocalDate.parse(raw);
        } catch (Exception e) {
            return LocalDate.now();
        }
    }

    private YearMonth parseMonth(String raw) {
        try {
            return YearMonth.parse(raw);
        } catch (Exception e) {
            return YearMonth.now();
        }
    }
}
