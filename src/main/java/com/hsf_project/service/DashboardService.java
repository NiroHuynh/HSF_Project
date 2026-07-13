package com.hsf_project.service;

import com.hsf_project.dto.dashboard.CinemaRevenueDto;
import com.hsf_project.dto.dashboard.DashboardStats;
import com.hsf_project.dto.dashboard.MonthlyRevenueDto;
import com.hsf_project.dto.dashboard.response.MovieRevenueRankingResponse;
import com.hsf_project.dto.dashboard.response.MovieStatsResponse;
import com.hsf_project.dto.dashboard.response.RevenueTrendResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface DashboardService {

    /* ---------- Dashboard tổng quan / báo cáo doanh thu (admin + manager) ---------- */

    /** Số liệu toàn hệ thống trong [from, to). */
    DashboardStats getStats(LocalDateTime from, LocalDateTime to);

    /** Số liệu giới hạn theo một rạp (trang Manager). */
    DashboardStats getStatsForCinema(LocalDateTime from, LocalDateTime to, Integer cinemaId);

    /** Doanh thu 12 tháng của một năm (tháng không có dữ liệu = 0). cinemaId null = toàn hệ thống. */
    List<MonthlyRevenueDto> getMonthlyRevenue(int year, Integer cinemaId);

    /** Doanh thu gộp theo từng chi nhánh rạp trong [from, to), giảm dần theo doanh thu. */
    List<CinemaRevenueDto> getRevenueByCinema(LocalDateTime from, LocalDateTime to);

    /* ---------- Dashboard quản lý phim (/admin/dashboard/movie) ---------- */

    MovieStatsResponse getMovieStats();

    List<RevenueTrendResponse> getRevenueTrend(String period, LocalDate from, LocalDate to);

    List<MovieRevenueRankingResponse> getMovieRevenueRanking(LocalDate from, LocalDate to, String status, String genre, String search);
}
