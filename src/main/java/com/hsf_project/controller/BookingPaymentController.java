package com.hsf_project.controller;

import com.hsf_project.entity.Combo;
import com.hsf_project.entity.PaymentMethod;
import com.hsf_project.entity.ShowTime;
import com.hsf_project.service.ComboService;
import com.hsf_project.service.PaymentMethodService;
import com.hsf_project.service.PromotionService;
import com.hsf_project.service.ShowTimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/booking")
public class BookingPaymentController {

    @Autowired
    private PaymentMethodService paymentMethodService;

    @Autowired
    private ComboService bookingComboService;

    @Autowired
    private PromotionService promotionService;

    @Autowired
    private ShowTimeService showTimeService;

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
        BigDecimal grandTotal = seatTotal.add(comboTotal).subtract(discount);

        List<PaymentMethod> paymentMethods = paymentMethodService.getActiveMethods();
        Long defaultPaymentMethodId = paymentMethods.isEmpty() ? null : paymentMethods.get(0).getId();

        model.addAttribute("showtimeId", showtimeId);
        model.addAttribute("seatIds", seatIds);
        model.addAttribute("selectedSeats", selectedSeats);
        model.addAttribute("seatTotal", seatTotal);
        model.addAttribute("selectedCombos", selectedCombos);
        model.addAttribute("comboQueryParams", comboQueryParams);
        model.addAttribute("comboTotal", comboTotal);
        model.addAttribute("discount", discount);
        model.addAttribute("grandTotal", grandTotal);
        model.addAttribute("showtimeInfo", loadShowtimeInfo(showtimeId));
        model.addAttribute("paymentMethods", paymentMethods);
        model.addAttribute("defaultPaymentMethodId", defaultPaymentMethodId);

        return "bookingPayment";
    }

    /**
     * MỚI: endpoint cho JS ở trang Thanh toán gọi AJAX khi người dùng bấm "Áp dụng".
     * Trả về JSON { valid, message, discountAmount, promotionId } lấy THẬT từ bảng promotion.
     */
    @PostMapping("/apply-promo")
    @ResponseBody
    public PromotionService.PromotionResult applyPromo(
            @RequestParam String code,
            @RequestParam BigDecimal orderAmount) {
        return promotionService.validate(code, orderAmount);
    }

    private ShowtimeInfo loadShowtimeInfo(Long showtimeId) {
        ShowTime showtime = showTimeService.getById(showtimeId);

        String startTimeLabel = showtime.getStartTime()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        String roomName = showtime.getRoom().getName();
        String formatLabel = "CINEMAX " + showtime.getRoom().getRoomType(); // vd: "CINEMAX IMAX"

        return new ShowtimeInfo(showtime.getMovie().getTitle(), showtime.getMovie().getPosterUrl(),
                startTimeLabel, roomName, formatLabel);
    }

    public record SelectedCombo(String name, int quantity, BigDecimal lineTotal) {
    }

    public record ShowtimeInfo(String movieTitle, String posterUrl, String startTime, String roomName, String formatLabel) {
    }
}