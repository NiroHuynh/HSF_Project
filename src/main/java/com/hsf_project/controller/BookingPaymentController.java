package com.hsf_project.controller;

import com.hsf_project.entity.Combo;
import com.hsf_project.entity.PaymentMethod;
import com.hsf_project.service.BookingComboService;
import com.hsf_project.service.BookingPaymentMethodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/booking")
public class BookingPaymentController {

    private static final BigDecimal SERVICE_FEE = new BigDecimal("3000");

    @Autowired
    private BookingPaymentMethodService bookingPaymentMethodService;

    @Autowired
    private BookingComboService bookingComboService;

    @GetMapping("/payment")
    public String showPaymentPage(
            @RequestParam Long showtimeId,
            @RequestParam String seatIds,
            @RequestParam(defaultValue = "0") BigDecimal seatTotal,
            @RequestParam Map<String, String> allParams,
            Model model) {

        List<String> selectedSeats = Arrays.stream(seatIds.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        List<SelectedCombo> selectedCombos = new ArrayList<>();
        Map<Long, Integer> comboQueryParams = new LinkedHashMap<>();
        BigDecimal comboTotal = BigDecimal.ZERO;

        for (Map.Entry<String, String> entry : allParams.entrySet()) {
            if (!entry.getKey().startsWith("combo_")) {
                continue;
            }
            Long comboId = Long.valueOf(entry.getKey().substring("combo_".length()));
            int qty = Integer.parseInt(entry.getValue());
            if (qty <= 0) {
                continue;
            }

            // ===== DỮ LIỆU THẬT: lấy combo từ DB qua Service, không còn map mock =====
            Combo combo;
            try {
                combo = bookingComboService.getById(comboId);
            } catch (IllegalArgumentException ex) {
                continue; // comboId không tồn tại (vd. bị sửa tay trên URL) -> bỏ qua, không cộng tiền
            }

            BigDecimal lineTotal = combo.getPrice().multiply(BigDecimal.valueOf(qty));
            selectedCombos.add(new SelectedCombo(combo.getName(), qty, lineTotal));
            comboQueryParams.put(comboId, qty);
            comboTotal = comboTotal.add(lineTotal);
        }

        BigDecimal discount = BigDecimal.ZERO;
        BigDecimal grandTotal = seatTotal.add(comboTotal).add(SERVICE_FEE).subtract(discount);

        List<PaymentMethod> paymentMethods = bookingPaymentMethodService.getActiveMethods();
        Long defaultPaymentMethodId = paymentMethods.isEmpty() ? null : paymentMethods.get(0).getId();

        model.addAttribute("showtimeId", showtimeId);
        model.addAttribute("seatIds", seatIds);
        model.addAttribute("selectedSeats", selectedSeats);
        model.addAttribute("seatTotal", seatTotal);
        model.addAttribute("selectedCombos", selectedCombos);
        model.addAttribute("comboQueryParams", comboQueryParams);
        model.addAttribute("comboTotal", comboTotal);
        model.addAttribute("serviceFee", SERVICE_FEE);
        model.addAttribute("discount", discount);
        model.addAttribute("grandTotal", grandTotal);
        model.addAttribute("showtimeInfo", mockShowtimeInfo(showtimeId)); // TODO: vẫn mock, xem ghi chú dưới
        model.addAttribute("paymentMethods", paymentMethods);
        model.addAttribute("defaultPaymentMethodId", defaultPaymentMethodId);

        return "bookingPayment";
    }

    // TODO: thay bằng BookingShowtimeService thật khi có Entity ShowTime/Movie/CinemaRoom/Cinema.
    private ShowtimeInfo mockShowtimeInfo(Long showtimeId) {
        return new ShowtimeInfo("Dune: Part Two", "20/05/2026 19:30", "Screen 04", "CINEMAX IMAX");
    }

    public record SelectedCombo(String name, int quantity, BigDecimal lineTotal) {
    }

    public record ShowtimeInfo(String movieTitle, String startTime, String roomName, String formatLabel) {
    }
}