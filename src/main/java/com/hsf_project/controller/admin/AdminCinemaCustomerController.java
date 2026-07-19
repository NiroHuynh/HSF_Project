package com.hsf_project.controller.admin;

import com.hsf_project.dto.admin.response.CinemaCustomerRow;
import com.hsf_project.dto.admin.response.CinemaCustomerStats;
import com.hsf_project.repository.BookingRepository;
import com.hsf_project.repository.CinemaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Trang tổng hợp khách hàng toàn hệ thống và lọc theo từng chi nhánh.
 *
 * "Khách của chi nhánh" được suy ra từ lịch sử đặt vé (booking → ticket → show_time →
 * cinema_room → cinema), vì bảng users không gắn FK tới rạp cho vai trò CUSTOMER.
 * Hệ quả: một khách đặt vé ở nhiều rạp được tính cho từng rạp, nên tổng các chi nhánh
 * lớn hơn hoặc bằng tổng toàn hệ thống — con số tổng lấy riêng bằng COUNT DISTINCT.
 */
@Controller
@RequestMapping("/admin/customers")
public class AdminCinemaCustomerController {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private CinemaRepository cinemaRepository;

    @GetMapping("/by-cinema")
    public String byCinema(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Integer cinemaId,
            Model model) {

        LocalDate end = to == null ? LocalDate.now() : to;
        LocalDate start = from == null ? end.minusMonths(1) : from;
        if (start.isAfter(end)) {
            LocalDate swap = start;
            start = end;
            end = swap;
        }
        LocalDateTime fromTime = start.atStartOfDay();
        LocalDateTime toTime = end.plusDays(1).atStartOfDay();

        List<CinemaCustomerStats> stats = new ArrayList<>();
        for (Object[] row : bookingRepository.countCustomersGroupedByCinema(fromTime, toTime)) {
            stats.add(new CinemaCustomerStats(
                    ((Number) row[0]).intValue(),
                    (String) row[1],
                    (String) row[2],
                    ((Number) row[3]).longValue(),
                    ((Number) row[4]).longValue()));
        }

        Long totalCustomers = bookingRepository.countDistinctCustomersAllCinemas(fromTime, toTime);
        long totalBookings = stats.stream().mapToLong(CinemaCustomerStats::bookingCount).sum();

        model.addAttribute("stats", stats);
        model.addAttribute("totalCustomers", totalCustomers == null ? 0L : totalCustomers);
        model.addAttribute("totalBookings", totalBookings);
        model.addAttribute("cinemaCount", stats.size());
        model.addAttribute("cinemas", cinemaRepository.findByIsDeletedFalseOrderByNameAsc());
        model.addAttribute("from", start);
        model.addAttribute("to", end);
        model.addAttribute("selectedCinemaId", cinemaId);
        model.addAttribute("active", "customers-cinema");

        // Chọn một chi nhánh thì hiện thêm danh sách khách của riêng chi nhánh đó.
        if (cinemaId != null) {
            List<CinemaCustomerRow> customers = new ArrayList<>();
            for (Object[] row : bookingRepository.findCustomersOfCinema(cinemaId, fromTime, toTime)) {
                customers.add(new CinemaCustomerRow(
                        ((Number) row[0]).longValue(),
                        (String) row[1],
                        (String) row[2],
                        (String) row[3],
                        ((Number) row[4]).longValue(),
                        (BigDecimal) row[5]));
            }
            model.addAttribute("customers", customers);
            stats.stream()
                    .filter(s -> s.cinemaId().equals(cinemaId))
                    .findFirst()
                    .ifPresent(s -> model.addAttribute("selectedCinemaName", s.cinemaName()));
        }

        return "admin/customers-by-cinema";
    }
}
