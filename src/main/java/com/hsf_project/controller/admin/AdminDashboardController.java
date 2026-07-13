package com.hsf_project.controller.admin;

import com.hsf_project.dto.dashboard.CinemaRevenueDto;
import com.hsf_project.dto.dashboard.DashboardStats;
import com.hsf_project.dto.dashboard.MonthlyRevenueDto;
import com.hsf_project.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

/** Khu quản trị: trang tổng quan + báo cáo doanh thu (chỉ ADMIN — AuthFilter chặn /admin/**). */
@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    @Autowired
    private DashboardService dashboardService;

    /** Tổng quan: số liệu nhanh của tháng hiện tại. */
    @GetMapping
    public String overview(Model model) {
        YearMonth thisMonth = YearMonth.now();
        LocalDateTime from = thisMonth.atDay(1).atStartOfDay();
        LocalDateTime to = from.plusMonths(1);

        model.addAttribute("stats", dashboardService.getStats(from, to));
        model.addAttribute("periodLabel", "Tháng " + thisMonth.getMonthValue() + "/" + thisMonth.getYear());
        model.addAttribute("activePage", "overview");
        return "admin/dashboard";
    }

    /**
     * Báo cáo doanh thu với 3 chế độ lọc:
     *   mode=day   + date=yyyy-MM-dd   -> doanh thu 1 ngày
     *   mode=month + month=yyyy-MM     -> doanh thu 1 tháng
     *   mode=year  + year=yyyy         -> doanh thu 1 năm (mặc định: năm hiện tại)
     */
    @GetMapping("/revenue")
    public String revenue(@RequestParam(name = "mode", defaultValue = "year") String mode,
                          @RequestParam(name = "date", required = false) String date,
                          @RequestParam(name = "month", required = false) String month,
                          @RequestParam(name = "year", required = false) Integer year,
                          Model model) {
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
            case "month" -> {
                YearMonth ym = parseMonth(month);
                from = ym.atDay(1).atStartOfDay();
                to = from.plusMonths(1);
                periodLabel = "Tháng " + ym.getMonthValue() + "/" + ym.getYear();
                chartYear = ym.getYear();
                model.addAttribute("month", ym.toString());
            }
            default -> {
                mode = "year";
                int y = (year != null) ? year : LocalDate.now().getYear();
                from = LocalDate.of(y, 1, 1).atStartOfDay();
                to = from.plusYears(1);
                periodLabel = "Năm " + y;
                chartYear = y;
                model.addAttribute("year", y);
            }
        }

        DashboardStats stats = dashboardService.getStats(from, to);
        List<MonthlyRevenueDto> monthly = dashboardService.getMonthlyRevenue(chartYear, null);
        List<CinemaRevenueDto> cinemaRevenues = dashboardService.getRevenueByCinema(from, to);

        model.addAttribute("stats", stats);
        model.addAttribute("monthlyData", monthly.stream().map(MonthlyRevenueDto::revenue).toList());
        model.addAttribute("chartYear", chartYear);
        model.addAttribute("cinemaRevenues", cinemaRevenues);
        model.addAttribute("mode", mode);
        model.addAttribute("periodLabel", periodLabel);
        model.addAttribute("activePage", "revenue");
        return "admin/revenue";
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
