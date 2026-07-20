package com.hsf_project.service.impl;

import com.hsf_project.dto.dashboard.CinemaRevenueDto;
import com.hsf_project.dto.dashboard.DashboardStats;
import com.hsf_project.dto.dashboard.MonthlyRevenueDto;
import com.hsf_project.dto.dashboard.response.MovieRevenueRankingResponse;
import com.hsf_project.dto.dashboard.response.MovieStatsResponse;
import com.hsf_project.dto.dashboard.response.RevenueTrendResponse;
import com.hsf_project.entity.Booking;
import com.hsf_project.entity.Movie;
import com.hsf_project.entity.enums.MovieStatus;
import com.hsf_project.entity.Ticket;
import com.hsf_project.mapper.DashboardMapper;
import com.hsf_project.repository.BookingComboRepository;
import com.hsf_project.repository.BookingRepository;
import com.hsf_project.repository.TicketRepository;
import com.hsf_project.repository.MovieRepository;
import com.hsf_project.service.DashboardService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class DashboardServiceImpl implements DashboardService {

    MovieRepository movieRepository;
    BookingRepository bookingRepository;
    DashboardMapper dashboardMapper;
    TicketRepository ticketRepository;
    BookingComboRepository bookingComboRepository;

    /* ---------- Dashboard tổng quan / báo cáo doanh thu (admin + manager) ---------- */

    @Override
    @Transactional(readOnly = true)
    public DashboardStats getStats(LocalDateTime from, LocalDateTime to) {
        return new DashboardStats(
                nz(bookingRepository.sumRevenue(from, to)),
                bookingRepository.countPaid(from, to),
                ticketRepository.countPaidTickets(from, to),
                nz(ticketRepository.ticketRevenue(from, to)),
                nz(bookingComboRepository.comboRevenue(from, to))
        );
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardStats getStatsForCinema(LocalDateTime from, LocalDateTime to, Integer cinemaId) {
        return new DashboardStats(
                nz(bookingRepository.sumRevenueByCinema(from, to, cinemaId)),
                bookingRepository.countPaidByCinema(from, to, cinemaId),
                ticketRepository.countPaidTicketsByCinema(from, to, cinemaId),
                nz(ticketRepository.ticketRevenueByCinema(from, to, cinemaId)),
                nz(bookingComboRepository.comboRevenueByCinema(from, to, cinemaId))
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<MonthlyRevenueDto> getMonthlyRevenue(int year, Integer cinemaId) {
        List<Object[]> rows = (cinemaId == null)
                ? bookingRepository.monthlyRevenue(year)
                : bookingRepository.monthlyRevenueByCinema(year, cinemaId);

        Map<Integer, BigDecimal> byMonth = new HashMap<>();
        for (Object[] row : rows) {
            byMonth.put(((Number) row[0]).intValue(), (BigDecimal) row[1]);
        }
        // Luôn trả đủ 12 tháng để biểu đồ không bị khuyết cột
        List<MonthlyRevenueDto> result = new ArrayList<>(12);
        for (int month = 1; month <= 12; month++) {
            result.add(new MonthlyRevenueDto(month, byMonth.getOrDefault(month, BigDecimal.ZERO)));
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CinemaRevenueDto> getRevenueByCinema(LocalDateTime from, LocalDateTime to) {
        // Mỗi row = [cinemaId, cinemaName, bookingId, finalAmount] (1 dòng / booking):
        // gộp theo rạp ở đây thay vì SUM trong JPQL để tránh nhân bản theo số vé.
        List<Object[]> rows = bookingRepository.paidBookingsWithCinema(from, to);

        Map<Integer, CinemaRevenueDto> byCinema = new LinkedHashMap<>();
        for (Object[] row : rows) {
            Integer cinemaId = ((Number) row[0]).intValue();
            String cinemaName = (String) row[1];
            BigDecimal amount = nz((BigDecimal) row[3]);
            byCinema.merge(cinemaId,
                    new CinemaRevenueDto(cinemaId, cinemaName, 1, amount),
                    (a, b) -> new CinemaRevenueDto(cinemaId, cinemaName,
                            a.bookings() + 1, a.revenue().add(amount)));
        }
        return byCinema.values().stream()
                .sorted(Comparator.comparing(CinemaRevenueDto::revenue).reversed())
                .toList();
    }

    private static BigDecimal nz(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    /* ---------- Dashboard quản lý phim (/admin/dashboard/movie) ---------- */

    @Override
    @Transactional(readOnly = true)
    public MovieStatsResponse getMovieStats() {
        long totalMovies = movieRepository.countByIsDeletedFalseOrIsDeletedNull();
        long nowShowingCount = movieRepository.countByStatusAndIsDeletedFalseOrIsDeletedNull(MovieStatus.NOW_SHOWING);
        long comingSoonCount = movieRepository.countByStatusAndIsDeletedFalseOrIsDeletedNull(MovieStatus.COMING_SOON);

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);
        List<Booking> dailyBookings = bookingRepository.findByBookingDateBetweenAndStatusAndIsDeletedFalse(
                todayStart, todayEnd, "PAID");
        BigDecimal dailyRevenue = sumFinalAmount(dailyBookings);

        List<Movie> allMovies = movieRepository.findByIsDeletedFalseOrIsDeletedNull();
        double averageRating = allMovies.stream()
                .filter(m -> m.getAverageRating() != null)
                .mapToDouble(m -> m.getAverageRating().doubleValue())
                .average()
                .orElse(0.0);
        averageRating = BigDecimal.valueOf(averageRating)
                .setScale(1, RoundingMode.HALF_UP).doubleValue();

        return MovieStatsResponse.builder()
                .totalMovies(totalMovies)
                .nowShowingCount(nowShowingCount)
                .comingSoonCount(comingSoonCount)
                .dailyRevenue(dailyRevenue)
                .averageRating(averageRating)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RevenueTrendResponse> getRevenueTrend(String period, LocalDate from, LocalDate to) {
        List<RevenueTrendResponse> result = new ArrayList<>();
        LocalDateTime fromDateTime = from.atStartOfDay();
        LocalDateTime toDateTime = to.atTime(LocalTime.MAX);

        switch (period != null ? period.toLowerCase() : "month") {
            case "quarter" -> {
                for (int year = from.getYear(); year <= to.getYear(); year++) {
                    for (int q = 1; q <= 4; q++) {
                        int startMonth = (q - 1) * 3 + 1;
                        int endMonth = q * 3;
                        LocalDate qStart = LocalDate.of(year, startMonth, 1);
                        LocalDate qEnd = YearMonth.of(year, endMonth).atEndOfMonth();
                        if (qStart.isAfter(to)) break;
                        if (qEnd.isBefore(from)) continue;
                        LocalDateTime qs = qStart.atStartOfDay();
                        LocalDateTime qe = qEnd.atTime(LocalTime.MAX);
                        List<Booking> periodBookings = bookingRepository.findByBookingDateBetweenAndStatusAndIsDeletedFalse(qs, qe, "PAID");
                        BigDecimal rev = sumFinalAmount(periodBookings);
                        result.add(RevenueTrendResponse.builder()
                                .period("Q" + q + "/" + year)
                                .revenue(rev)
                                .target(null)
                                .build());
                    }
                }
            }
            case "year" -> {
                for (int year = from.getYear(); year <= to.getYear(); year++) {
                    LocalDate yStart = LocalDate.of(year, 1, 1);
                    LocalDate yEnd = LocalDate.of(year, 12, 31);
                    if (yStart.isAfter(to)) break;
                    if (yEnd.isBefore(from)) continue;
                    List<Booking> yearBookings = bookingRepository.findByBookingDateBetweenAndStatusAndIsDeletedFalse(
                            yStart.atStartOfDay(), yEnd.atTime(LocalTime.MAX), "PAID");
                    BigDecimal rev = sumFinalAmount(yearBookings);
                    result.add(RevenueTrendResponse.builder()
                            .period(String.valueOf(year))
                            .revenue(rev)
                            .target(null)
                            .build());
                }
            }
            default -> {
                YearMonth current = YearMonth.from(from);
                YearMonth end = YearMonth.from(to);
                while (!current.isAfter(end)) {
                    LocalDate mStart = current.atDay(1);
                    LocalDate mEnd = current.atEndOfMonth();
                    LocalDateTime ms = mStart.atStartOfDay();
                    LocalDateTime me = mEnd.atTime(LocalTime.MAX);
                    List<Booking> monthBookings = bookingRepository.findByBookingDateBetweenAndStatusAndIsDeletedFalse(ms, me, "PAID");
                    BigDecimal rev = sumFinalAmount(monthBookings);
                    result.add(RevenueTrendResponse.builder()
                            .period(current.format(DateTimeFormatter.ofPattern("MM/yyyy")))
                            .revenue(rev)
                            .target(null)
                            .build());
                    current = current.plusMonths(1);
                }
            }
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovieRevenueRankingResponse> getMovieRevenueRanking(LocalDate from, LocalDate to, String status, String genre, String search) {
        LocalDateTime fromDateTime = from.atStartOfDay();
        LocalDateTime toDateTime = to.atTime(LocalTime.MAX);

        MovieStatus movieStatus = parseMovieStatus(status);
        Integer genreId = parseGenreId(genre);
        String keyword = (search != null && !search.trim().isEmpty()) ? search.trim().toLowerCase() : null;

        List<Booking> bookings = bookingRepository
                .findByBookingDateBetweenAndStatusAndIsDeletedFalse(fromDateTime, toDateTime, "PAID");

        Map<Integer, MovieRevenueGroup> groupMap = new LinkedHashMap<>();
        for (Booking booking : bookings) {
            if (booking.getTickets().isEmpty()) continue;
            Movie movie = booking.getTickets().get(0).getShowtime().getMovie();
            if (Boolean.TRUE.equals(movie.getIsDeleted())) continue;
            if (movieStatus != null && movie.getStatus() != movieStatus) continue;
            if (genreId != null && movie.getGenres().stream().noneMatch(g -> g.getId().equals(genreId))) continue;
            if (!matchesKeyword(movie, keyword)) continue;

            MovieRevenueGroup group = groupMap.computeIfAbsent(movie.getId(), k -> new MovieRevenueGroup(movie));
            group.addRevenue(booking.getFinalAmount());
            booking.getTickets().forEach(t -> group.addTicketId(t.getId()));
        }

        movieRepository.findByIsDeletedFalseOrIsDeletedNull().stream()
                .filter(m -> movieStatus == null || m.getStatus() == movieStatus)
                .filter(m -> genreId == null || m.getGenres().stream().anyMatch(g -> g.getId().equals(genreId)))
                .filter(m -> matchesKeyword(m, keyword))
                .forEach(m -> groupMap.putIfAbsent(m.getId(), new MovieRevenueGroup(m)));

        int[] rank = {0};
        return groupMap.values().stream()
                .sorted((a, b) -> b.revenue.compareTo(a.revenue))
                .map(g -> dashboardMapper.toRevenueRankingResponse(g.movie, g.revenue, g.ticketIds))
                .peek(r -> r.setRank(++rank[0]))
                .collect(Collectors.toList());
    }

    private boolean matchesKeyword(Movie movie, String keyword) {
        if (keyword == null) return true;
        return (movie.getTitle() != null && movie.getTitle().toLowerCase().contains(keyword))
                || (movie.getDirector() != null && movie.getDirector().toLowerCase().contains(keyword))
                || (movie.getCast() != null && movie.getCast().toLowerCase().contains(keyword));
    }

    private Integer parseGenreId(String genre) {
        if (genre != null && !genre.isBlank()) {
            try {
                return Integer.valueOf(genre);
            } catch (NumberFormatException ignored) {}
        }
        return null;
    }

    private BigDecimal sumFinalAmount(List<Booking> bookings) {
        return bookings.stream()
                .map(Booking::getFinalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private MovieStatus parseMovieStatus(String status) {
        if (status != null && !status.isBlank()) {
            try {
                return MovieStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }
        return null;
    }

    private static class MovieRevenueGroup {
        Movie movie;
        BigDecimal revenue = BigDecimal.ZERO;
        Set<Long> ticketIds = new HashSet<>();

        MovieRevenueGroup(Movie movie) {
            this.movie = movie;
        }

        void addRevenue(BigDecimal amount) {
            revenue = revenue.add(amount != null ? amount : BigDecimal.ZERO);
        }

        void addTicketId(Long ticketId) {
            ticketIds.add(ticketId);
        }
    }
}
