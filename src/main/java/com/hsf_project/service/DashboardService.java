package com.hsf_project.service;

import com.hsf_project.dto.dashboard.CinemaRevenueDto;
import com.hsf_project.dto.dashboard.DashboardStats;
import com.hsf_project.dto.dashboard.MonthlyRevenueDto;

import java.time.LocalDateTime;
import java.util.List;

public interface DashboardService {

    /** Số liệu toàn hệ thống trong [from, to). */
    DashboardStats getStats(LocalDateTime from, LocalDateTime to);

    /** Số liệu giới hạn theo một rạp (trang Manager). */
    DashboardStats getStatsForCinema(LocalDateTime from, LocalDateTime to, Integer cinemaId);

    /** Doanh thu 12 tháng của một năm (tháng không có dữ liệu = 0). cinemaId null = toàn hệ thống. */
    List<MonthlyRevenueDto> getMonthlyRevenue(int year, Integer cinemaId);

    /** Doanh thu gộp theo từng chi nhánh rạp trong [from, to), giảm dần theo doanh thu. */
    List<CinemaRevenueDto> getRevenueByCinema(LocalDateTime from, LocalDateTime to);
}
