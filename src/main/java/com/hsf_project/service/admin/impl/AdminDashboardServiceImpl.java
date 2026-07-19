package com.hsf_project.service.admin.impl;

import com.hsf_project.repository.admin.AdminMovieRepository;
import com.hsf_project.repository.admin.AdminPromotionRepository;
import com.hsf_project.repository.admin.AdminUserRepository;
import com.hsf_project.service.admin.AdminDashboardService;
import org.springframework.stereotype.Service;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class AdminDashboardServiceImpl implements AdminDashboardService {
    private final AdminUserRepository users;
    private final AdminMovieRepository movies;
    private final AdminPromotionRepository promotions;

    public AdminDashboardServiceImpl(AdminUserRepository users, AdminMovieRepository movies,
                                     AdminPromotionRepository promotions) {
        this.users = users;
        this.movies = movies;
        this.promotions = promotions;
    }

    public Map<String, Long> getOverview() {
        Map<String, Long> result = new LinkedHashMap<>();
        result.put("totalUsers", users.countActiveUsers());
        result.put("customers", users.countByRole("CUSTOMER"));
        result.put("staff", users.countByRole("MANAGER"));
        result.put("movies", (long) movies.findAllVisible().size());
        result.put("vouchers", (long) promotions.findAllVisible().size());
        return result;
    }
}
