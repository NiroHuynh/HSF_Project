package com.hsf_project.controller.admin;

import com.hsf_project.dto.common.ApiResponse;
import com.hsf_project.dto.dashboard.response.MovieRevenueRankingResponse;
import com.hsf_project.dto.dashboard.response.MovieStatsResponse;
import com.hsf_project.dto.dashboard.response.RevenueTrendResponse;
import com.hsf_project.service.DashboardService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/admin/dashboard/movie")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class AdminDashboardController {

    DashboardService dashboardService;

    @GetMapping
    public ApiResponse<MovieStatsResponse> getMovieStats() {
        return ApiResponse.success(dashboardService.getMovieStats());
    }

    @GetMapping("/trend")
    public ApiResponse<List<RevenueTrendResponse>> getRevenueTrend(
            @RequestParam(defaultValue = "month") String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        if (from == null) from = LocalDate.now().minusMonths(11).withDayOfMonth(1);
        if (to == null) to = LocalDate.now();
        if (from.isAfter(to)) {
            LocalDate tmp = from;
            from = to;
            to = tmp;
        }
        return ApiResponse.success(dashboardService.getRevenueTrend(period, from, to));
    }

    @GetMapping("/ranking")
    public ApiResponse<List<MovieRevenueRankingResponse>> getMovieRevenueRanking(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String search) {
        if (from == null) from = LocalDate.now().minusMonths(1);
        if (to == null) to = LocalDate.now();
        if (from.isAfter(to)) {
            LocalDate tmp = from;
            from = to;
            to = tmp;
        }
        return ApiResponse.success(dashboardService.getMovieRevenueRanking(from, to, status, genre, search));
    }
}
