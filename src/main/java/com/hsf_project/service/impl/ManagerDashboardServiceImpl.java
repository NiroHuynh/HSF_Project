package com.hsf_project.service.impl;

import com.hsf_project.entity.Booking;
import com.hsf_project.entity.BookingCombo;
import com.hsf_project.entity.Ticket;
import com.hsf_project.repository.BookingRepository;
import com.hsf_project.repository.ShowTimeRepository;
import com.hsf_project.service.ManagerDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ManagerDashboardServiceImpl implements ManagerDashboardService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ShowTimeRepository showTimeRepository;

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FMT  = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final List<String> MONTH_LABELS =
            List.of("T1","T2","T3","T4","T5","T6","T7","T8","T9","T10","T11","T12");

    // ── getStats ──────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getStats(Integer cinemaId, LocalDateTime from, LocalDateTime to, String mode) {
        BigDecimal revenue = bookingRepository.getTotalRevenueByCinema(cinemaId, from, to);
        Long showtimes     = showTimeRepository.countByCinemaAndDateRange(cinemaId, from, to);
        Long tickets       = bookingRepository.countBookingsByCinema(cinemaId, from, to);
        Long customers     = bookingRepository.countCustomersByCinema(cinemaId, from, to);

        NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
        nf.setMaximumFractionDigits(0);
        String revenueFormatted = nf.format(revenue != null ? revenue : BigDecimal.ZERO) + " đ";

        List<String> chartLabels;
        long[]       chartData;

        switch (mode) {
            case "today": {
                chartLabels = List.of(from.format(DateTimeFormatter.ofPattern("dd/MM")));
                long todayRev = revenue != null ? revenue.longValue() : 0L;
                chartData = new long[]{ todayRev };
                break;
            }
            case "month": {
                chartLabels = List.of("Tuần 1", "Tuần 2", "Tuần 3", "Tuần 4");
                chartData   = new long[4];
                List<Object[]> weekly = bookingRepository.getWeeklyRevenueByCinema(cinemaId, from, to);
                for (Object[] row : weekly) {
                    int week = ((Number) row[0]).intValue() - 1;
                    if (week >= 0 && week < 4) chartData[week] = ((Number) row[1]).longValue();
                }
                break;
            }
            case "quarter": {
                chartLabels = List.of("Q1", "Q2", "Q3", "Q4");
                chartData   = new long[4];
                List<Object[]> quarterData = bookingRepository.getQuarterlyRevenueByCinema(cinemaId, from, to);
                for (Object[] row : quarterData) {
                    int q = ((Number) row[0]).intValue() - 1;
                    if (q >= 0 && q < 4) chartData[q] = ((Number) row[1]).longValue();
                }
                break;
            }
            default: {
                chartLabels = MONTH_LABELS;
                chartData   = new long[12];
                List<Object[]> monthly = bookingRepository.getMonthlyRevenueByCinema(cinemaId, from, to);
                for (Object[] row : monthly) {
                    int month = ((Number) row[0]).intValue() - 1;
                    if (month >= 0 && month < 12) chartData[month] = ((Number) row[1]).longValue();
                }
                break;
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("revenue",     revenueFormatted);
        result.put("showtimes",   showtimes  != null ? showtimes  : 0L);
        result.put("tickets",     tickets    != null ? tickets    : 0L);
        result.put("customers",   customers  != null ? customers  : 0L);
        result.put("chartLabels", chartLabels);
        result.put("chartData",   chartData);
        return result;
    }

    // ── searchBooking ─────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> searchBooking(String bookingCode, Integer cinemaId) {
        Optional<Booking> opt = bookingRepository
                .findByCodeAndCinemaForSearch(bookingCode.trim(), cinemaId);

        if (opt.isEmpty()) {
            Map<String, Object> notFound = new LinkedHashMap<>();
            notFound.put("found", false);
            return notFound;
        }

        Booking b = opt.get();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("found", true);
        result.put("bookingCode", b.getBookingCode());
        result.put("status",      b.getStatus());
        result.put("customerName",  b.getUser().getLastName() + " " + b.getUser().getFirstName());
        result.put("customerPhone", b.getUser().getPhoneNumber() != null ? b.getUser().getPhoneNumber() : "—");

        List<Ticket> tickets = b.getTickets();
        if (!tickets.isEmpty()) {
            var showtime = tickets.get(0).getShowtime();
            var movie    = showtime.getMovie();
            var room     = showtime.getRoom();
            result.put("movieTitle",  movie.getTitle());
            result.put("moviePoster", movie.getPosterUrl() != null ? movie.getPosterUrl() : "");
            result.put("roomName",    room.getName() + " · " + room.getRoomType());
            result.put("showTime",    showtime.getStartTime().format(TIME_FMT)
                    + " – " + showtime.getEndTime().format(TIME_FMT));
            result.put("showDate",    showtime.getStartTime().format(DATE_FMT));
        }

        List<Map<String, String>> ticketItems = new ArrayList<>();
        for (Ticket t : tickets) {
            Map<String, String> item = new LinkedHashMap<>();
            item.put("seatCode", t.getSeat().getSeatCode());
            item.put("seatType", t.getSeat().getType());
            item.put("status",   t.getStatus());
            ticketItems.add(item);
        }
        result.put("tickets", ticketItems);

        NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
        nf.setMaximumFractionDigits(0);

        List<Map<String, String>> comboItems = new ArrayList<>();
        for (BookingCombo bc : b.getBookingCombos()) {
            Map<String, String> item = new LinkedHashMap<>();
            item.put("name",       bc.getCombo().getName());
            item.put("quantity",   String.valueOf(bc.getQuantity()));
            item.put("unitPrice",  nf.format(bc.getUnitPrice()) + " đ");
            item.put("totalPrice", nf.format(bc.getTotalPrice()) + " đ");
            comboItems.add(item);
        }
        result.put("combos", comboItems);

        if (b.getPromotion() != null) {
            result.put("promoCode", b.getPromotion().getCode());
            result.put("promoName", b.getPromotion().getName());
        } else {
            result.put("promoCode", null);
            result.put("promoName", null);
        }

        result.put("totalAmount",    nf.format(b.getTotalAmount())    + " đ");
        result.put("discountAmount", nf.format(b.getDiscountAmount()) + " đ");
        result.put("finalAmount",    nf.format(b.getFinalAmount())    + " đ");

        return result;
    }

    // ── exportBooking ─────────────────────────────────────────────────────────

    @Override
    @Transactional
    public Map<String, Object> exportBooking(String bookingCode, Integer cinemaId) {
        Optional<Booking> opt = bookingRepository
                .findByCodeAndCinemaForSearch(bookingCode.trim(), cinemaId);

        if (opt.isEmpty()) {
            return Map.of("success", false, "message", "Không tìm thấy booking tại chi nhánh của bạn.");
        }

        Booking b = opt.get();

        // Chỉ được xuất khi đã thanh toán
        if ("EXPORTED".equals(b.getStatus())) {
            return Map.of("success", false, "message", "Vé này đã được xuất trước đó.");
        }
        if (!"CONFIRMED".equals(b.getStatus())) {
            return Map.of("success", false, "message", "Chỉ có thể xuất vé khi booking đã thanh toán (CONFIRMED).");
        }

        b.setStatus("EXPORTED");
        bookingRepository.save(b);
        return Map.of("success", true);
    }
}