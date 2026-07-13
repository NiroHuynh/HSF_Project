package com.hsf_project.service.impl;

import com.hsf_project.dto.dashboard.CinemaRevenueDto;
import com.hsf_project.dto.dashboard.DashboardStats;
import com.hsf_project.dto.dashboard.MonthlyRevenueDto;
import com.hsf_project.repository.BookingComboRepository;
import com.hsf_project.repository.BookingRepository;
import com.hsf_project.repository.TicketRepository;
import com.hsf_project.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private BookingComboRepository bookingComboRepository;

    @Override
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
}
