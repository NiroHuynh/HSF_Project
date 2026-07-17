package com.hsf_project.service;

import com.hsf_project.dto.dashboard.response.MovieRevenueRankingResponse;
import com.hsf_project.dto.dashboard.response.MovieStatsResponse;
import com.hsf_project.dto.dashboard.response.RevenueTrendResponse;

import java.time.LocalDate;
import java.util.List;

public interface DashboardService {
    MovieStatsResponse getMovieStats();

    List<RevenueTrendResponse> getRevenueTrend(String period, LocalDate from, LocalDate to);

    List<MovieRevenueRankingResponse> getMovieRevenueRanking(LocalDate from, LocalDate to, String status, String genre, String search);
}
