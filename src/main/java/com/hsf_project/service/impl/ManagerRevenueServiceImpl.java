package com.hsf_project.service.impl;

import com.hsf_project.repository.BookingRepository;
import com.hsf_project.service.ManagerRevenueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ManagerRevenueServiceImpl implements ManagerRevenueService {

    @Autowired
    private BookingRepository bookingRepository;

    private static final List<String> MONTH_LABELS =
            List.of("T1","T2","T3","T4","T5","T6","T7","T8","T9","T10","T11","T12");
    private static final List<String> WEEK_LABELS  =
            List.of("Tuần 1","Tuần 2","Tuần 3","Tuần 4");
    private static final List<String> QUARTER_LABELS =
            List.of("Q1","Q2","Q3","Q4");

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getRevenueStats(Integer cinemaId,
                                               LocalDateTime from,
                                               LocalDateTime to,
                                               String mode) {
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
        nf.setMaximumFractionDigits(0);

        // ── Stat cards ────────────────────────────────────────────────────────
        BigDecimal totalRev = bookingRepository.getTotalRevenueByCinema(cinemaId, from, to);
        BigDecimal comboRev = bookingRepository.getTotalComboRevenueByCinema(cinemaId, from, to);
        if (totalRev == null) totalRev = BigDecimal.ZERO;
        if (comboRev == null) comboRev = BigDecimal.ZERO;

        BigDecimal ticketRev = totalRev.subtract(comboRev);
        if (ticketRev.compareTo(BigDecimal.ZERO) < 0) ticketRev = BigDecimal.ZERO;

        Long ticketCount = bookingRepository.countTicketsSoldByCinema(cinemaId, from, to);
        Long comboCount  = bookingRepository.countCombosSoldByCinema(cinemaId, from, to);
        if (ticketCount == null) ticketCount = 0L;
        if (comboCount  == null) comboCount  = 0L;

        // Tỉ lệ %
        String ticketPct = "0%", comboPct = "0%";
        if (totalRev.compareTo(BigDecimal.ZERO) > 0) {
            ticketPct = ticketRev.multiply(BigDecimal.valueOf(100))
                    .divide(totalRev, 1, RoundingMode.HALF_UP) + "%";
            comboPct  = comboRev.multiply(BigDecimal.valueOf(100))
                    .divide(totalRev, 1, RoundingMode.HALF_UP) + "%";
        }

        // ── Dữ liệu biểu đồ stacked theo mode ────────────────────────────────
        List<String> chartLabels;
        long[] ticketChartData;
        long[] comboChartData;

        switch (mode) {

            case "today": {
                chartLabels     = List.of(from.format(DateTimeFormatter.ofPattern("dd/MM")));
                ticketChartData = new long[]{ ticketRev.longValue() };
                comboChartData  = new long[]{ comboRev.longValue() };
                break;
            }

            case "month": {
                chartLabels    = WEEK_LABELS;
                ticketChartData = buildWeekData(cinemaId, from, to, false);
                comboChartData  = buildWeekData(cinemaId, from, to, true);
                break;
            }

            case "quarter": {
                chartLabels    = QUARTER_LABELS;
                ticketChartData = buildQuarterData(cinemaId, from, to, false);
                comboChartData  = buildQuarterData(cinemaId, from, to, true);
                break;
            }

            default: { // year
                chartLabels    = MONTH_LABELS;
                ticketChartData = buildMonthData(cinemaId, from, to, false);
                comboChartData  = buildMonthData(cinemaId, from, to, true);
                break;
            }
        }

        // ── Top movies ────────────────────────────────────────────────────────
        List<Object[]> topRaw = bookingRepository.getTopMoviesByCinema(cinemaId, from, to);
        long maxRev = topRaw.isEmpty() ? 1L :
                ((Number) topRaw.get(0)[2]).longValue(); // phim #1 có doanh thu cao nhất

        List<Map<String, Object>> topMovies = new ArrayList<>();
        int rank = 1;
        for (Object[] row : topRaw) {
            String  title     = (String) row[0];
            long    tickets   = ((Number) row[1]).longValue();
            long    revenue   = ((Number) row[2]).longValue();
            int     barWidth  = maxRev > 0 ? (int)(revenue * 100L / maxRev) : 0;

            Map<String, Object> m = new LinkedHashMap<>();
            m.put("rank",     rank++);
            m.put("title",    title);
            m.put("tickets",  nf.format(tickets));
            m.put("revenue",  nf.format(revenue) + " đ");
            m.put("barWidth", barWidth);
            topMovies.add(m);
        }

        // ── Kết quả ──────────────────────────────────────────────────────────
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalRevenue",    nf.format(totalRev)  + " đ");
        result.put("ticketRevenue",   nf.format(ticketRev) + " đ");
        result.put("comboRevenue",    nf.format(comboRev)  + " đ");
        result.put("ticketCount",     nf.format(ticketCount));
        result.put("comboCount",      nf.format(comboCount));
        result.put("ticketPercent",   ticketPct);
        result.put("comboPercent",    comboPct);
        result.put("chartLabels",     chartLabels);
        result.put("ticketChartData", ticketChartData);
        result.put("comboChartData",  comboChartData);
        result.put("topMovies",       topMovies);
        return result;
    }

    // ── Helpers build chart data ──────────────────────────────────────────────

    private long[] buildMonthData(Integer cinemaId, LocalDateTime from, LocalDateTime to, boolean isCombo) {
        long[] data = new long[12];
        List<Object[]> rows = isCombo
                ? bookingRepository.getMonthlyComboRevenueByCinema(cinemaId, from, to)
                : bookingRepository.getMonthlyRevenueByCinema(cinemaId, from, to);
        // Nếu là ticket: total - combo per month
        long[] comboData = new long[12];
        if (!isCombo) {
            List<Object[]> comboRows = bookingRepository.getMonthlyComboRevenueByCinema(cinemaId, from, to);
            for (Object[] r : comboRows) {
                int idx = ((Number) r[0]).intValue() - 1;
                if (idx >= 0 && idx < 12) comboData[idx] = ((Number) r[1]).longValue();
            }
        }
        for (Object[] r : rows) {
            int idx = ((Number) r[0]).intValue() - 1;
            if (idx >= 0 && idx < 12) {
                long val = ((Number) r[1]).longValue();
                data[idx] = isCombo ? val : Math.max(0, val - comboData[idx]);
            }
        }
        return data;
    }

    private long[] buildWeekData(Integer cinemaId, LocalDateTime from, LocalDateTime to, boolean isCombo) {
        long[] data = new long[4];
        List<Object[]> rows = isCombo
                ? bookingRepository.getWeeklyComboRevenueByCinema(cinemaId, from, to)
                : bookingRepository.getWeeklyRevenueByCinema(cinemaId, from, to);
        long[] comboData = new long[4];
        if (!isCombo) {
            List<Object[]> comboRows = bookingRepository.getWeeklyComboRevenueByCinema(cinemaId, from, to);
            for (Object[] r : comboRows) {
                int idx = ((Number) r[0]).intValue() - 1;
                if (idx >= 0 && idx < 4) comboData[idx] = ((Number) r[1]).longValue();
            }
        }
        for (Object[] r : rows) {
            int idx = ((Number) r[0]).intValue() - 1;
            if (idx >= 0 && idx < 4) {
                long val = ((Number) r[1]).longValue();
                data[idx] = isCombo ? val : Math.max(0, val - comboData[idx]);
            }
        }
        return data;
    }

    private long[] buildQuarterData(Integer cinemaId, LocalDateTime from, LocalDateTime to, boolean isCombo) {
        long[] data = new long[4];
        List<Object[]> rows = isCombo
                ? bookingRepository.getQuarterlyComboRevenueByCinema(cinemaId, from, to)
                : bookingRepository.getQuarterlyRevenueByCinema(cinemaId, from, to);
        long[] comboData = new long[4];
        if (!isCombo) {
            List<Object[]> comboRows = bookingRepository.getQuarterlyComboRevenueByCinema(cinemaId, from, to);
            for (Object[] r : comboRows) {
                int idx = ((Number) r[0]).intValue() - 1;
                if (idx >= 0 && idx < 4) comboData[idx] = ((Number) r[1]).longValue();
            }
        }
        for (Object[] r : rows) {
            int idx = ((Number) r[0]).intValue() - 1;
            if (idx >= 0 && idx < 4) {
                long val = ((Number) r[1]).longValue();
                data[idx] = isCombo ? val : Math.max(0, val - comboData[idx]);
            }
        }
        return data;
    }
}