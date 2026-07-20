package com.hsf_project.controller.admin;

import com.hsf_project.dto.customer.response.ActiveCustomerResponse;
import com.hsf_project.dto.customer.response.CustomerDetailResponse;
import com.hsf_project.dto.customer.response.CustomerGrowthResponse;
import com.hsf_project.dto.customer.response.CustomerSummaryResponse;
import com.hsf_project.dto.customer.response.TransactionResponse;
import com.hsf_project.dto.dashboard.response.MovieRevenueRankingResponse;
import com.hsf_project.dto.dashboard.response.MovieStatsResponse;
import com.hsf_project.dto.movie.response.GenreDTO;
import com.hsf_project.entity.AgeRating;
import com.hsf_project.entity.Movie;
import com.hsf_project.mapper.GenreMapper;
import com.hsf_project.mapper.MovieAdminMapper;
import com.hsf_project.repository.ShowTimeRepository;
import com.hsf_project.repository.movie.GenreRepository;
import com.hsf_project.service.CloudinaryService;
import com.hsf_project.service.CustomerDashboardService;
import com.hsf_project.service.DashboardService;
import com.hsf_project.service.MovieService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static java.util.stream.Collectors.toList;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class AdminPageController {

    MovieService movieService;
    DashboardService dashboardService;
    CustomerDashboardService customerDashboardService;
    GenreRepository genreRepository;
    GenreMapper genreMapper;
    MovieAdminMapper movieAdminMapper;
    CloudinaryService cloudinaryService;
    ShowTimeRepository showTimeRepository;

    @GetMapping("/customers/dashboard")
    public String customerDashboard(Model model) {
        model.addAttribute("activePage", "customers");

        LocalDate now = LocalDate.now();
        LocalDate defaultFrom = now.minusMonths(1);

        DateTimeFormatter isoFmt = DateTimeFormatter.ISO_LOCAL_DATE;
        model.addAttribute("fromDateParam", defaultFrom.format(isoFmt));
        model.addAttribute("toDateParam", now.format(isoFmt));

        CustomerSummaryResponse summary = customerDashboardService.getCustomerSummary(defaultFrom, now);
        model.addAttribute("totalCustomers", summary.getTotalCustomers());
        model.addAttribute("newCustomers", summary.getNewCustomers());
        model.addAttribute("monthlyVisits", summary.getMonthlyVisits());

        BigDecimal avg = summary.getAverageSpending();
        String avgStr;
        if (avg.compareTo(BigDecimal.valueOf(1_000_000_000)) >= 0)
            avgStr = avg.divide(BigDecimal.valueOf(1_000_000_000), 2, RoundingMode.HALF_UP) + "B";
        else if (avg.compareTo(BigDecimal.valueOf(1_000_000)) >= 0)
            avgStr = avg.divide(BigDecimal.valueOf(1_000_000), 1, RoundingMode.HALF_UP) + "M";
        else if (avg.compareTo(BigDecimal.valueOf(1_000)) >= 0)
            avgStr = avg.divide(BigDecimal.valueOf(1_000), 1, RoundingMode.HALF_UP) + "k";
        else
            avgStr = avg.toString();
        model.addAttribute("avgSpending", avgStr);

        LocalDate chartFrom = now.minusMonths(11).withDayOfMonth(1);
        List<CustomerGrowthResponse> growth = customerDashboardService.getCustomerGrowth("month", chartFrom, now);
        model.addAttribute("chartLabels", growth.stream().map(CustomerGrowthResponse::getLabel).toList());
        model.addAttribute("chartDataList", growth.stream().map(CustomerGrowthResponse::getValue).toList());

        Page<ActiveCustomerResponse> initialCustomers = customerDashboardService.getActiveCustomers(
                PageRequest.of(0, 6, Sort.by(Sort.Order.desc("bookingCount"))));
        model.addAttribute("customers", initialCustomers.getContent());

        return "CustomerDashboard";
    }

    @GetMapping("/customers/{id}/detail")
    public String customerDetail(@PathVariable Long id, Model model) {
        model.addAttribute("activePage", "customers");
        CustomerDetailResponse detail = customerDashboardService.getCustomerDetail(id);
        model.addAttribute("customer", detail);
        List<TransactionResponse> transactions = customerDashboardService.getRecentTransactions(id, 20);
        model.addAttribute("transactions", transactions);
        return "CustomerDetail";
    }

    @GetMapping("/movies/dashboard")
    public String movieDashboard(Model model) {
        model.addAttribute("activePage", "dashboard");
        MovieStatsResponse stats = dashboardService.getMovieStats();
        model.addAttribute("totalMovies", stats.getTotalMovies());
        model.addAttribute("nowShowingCount", stats.getNowShowingCount());
        model.addAttribute("dailyRevenue", stats.getDailyRevenue());
        model.addAttribute("avgRating", stats.getAverageRating());

        LocalDate now = LocalDate.now();
        LocalDate defaultFrom = now.minusMonths(1);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        model.addAttribute("fromDate", defaultFrom.format(fmt));
        model.addAttribute("toDate", now.format(fmt));

        List<MovieRevenueRankingResponse> ranking = dashboardService.getMovieRevenueRanking(
                defaultFrom, now, null, null, null);
        List<Map<String, Object>> movies = ranking.stream().map(r -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", r.getMovieId());
            m.put("name", r.getTitle());
            m.put("image", r.getPosterUrl());
            m.put("revenue", r.getRevenue());
            m.put("ticketsSold", r.getTicketsSold());
            return m;
        }).collect(toList());
        model.addAttribute("movies", movies);

        List<GenreDTO> genreDTOs = genreMapper.toGenreDTOList(genreRepository.findByIsDeletedFalse());
        model.addAttribute("genres", genreDTOs);
        return "movie-dashboard";
    }

    @GetMapping("/movies/add")
    public String addMovieForm(Model model) {
        List<GenreDTO> genreDTOs = genreMapper.toGenreDTOList(genreRepository.findByIsDeletedFalse());
        model.addAttribute("genres", genreDTOs);
        model.addAttribute("ageRatings", AgeRating.values());
        model.addAttribute("mode", "add");
        model.addAttribute("activePage", "movie-form");
        model.addAttribute("posterOptimized", "");
        model.addAttribute("hasShowtimes", false);
        return "movie-form";
    }

    @GetMapping("/movies/{id}/edit")
    public String editMovieForm(@PathVariable Integer id, Model model) {
        Movie movie = movieService.getMovieByIdAdmin(id);
        List<GenreDTO> genreDTOs = genreMapper.toGenreDTOList(genreRepository.findByIsDeletedFalse());
        model.addAttribute("genres", genreDTOs);
        model.addAttribute("ageRatings", AgeRating.values());
        model.addAttribute("movie", movieAdminMapper.toMovieResponse(movie));
        model.addAttribute("mode", "edit");
        model.addAttribute("activePage", "movie-form");

        String genreIdsStr = movie.getGenres().stream()
                .map(g -> String.valueOf(g.getId()))
                .collect(java.util.stream.Collectors.joining(","));
        model.addAttribute("genreIdsStr", genreIdsStr);
        model.addAttribute("directorStr", movie.getDirector());
        model.addAttribute("castStr", movie.getCast());

        model.addAttribute("posterOptimized", cloudinaryService.getResizedUrl(movie.getPosterUrl(), 400, 600));
        model.addAttribute("hasShowtimes", !showTimeRepository.findByMovieIdAndIsDeletedFalse(id).isEmpty());

        return "movie-form";
    }
}
