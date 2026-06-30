package com.hsf_project.controller;

import com.hsf_project.service.BookingConfirmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/booking")
public class BookingConfirmController {

    @Autowired
    private BookingConfirmService bookingConfirmService;

    @PostMapping("/confirm")
    public String confirmBooking(
            @RequestParam Long showtimeId,
            @RequestParam String seatIds,
            @RequestParam BigDecimal discountAmount,
            @RequestParam(required = false) String promotionId,
            @RequestParam Long paymentMethod,
            @RequestParam Map<String, String> allParams,
            Model model) {

        List<String> seatCodes = Arrays.stream(seatIds.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        Map<Long, Integer> comboQuantities = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : allParams.entrySet()) {
            if (!entry.getKey().startsWith("combo_")) {
                continue;
            }
            Long comboId = Long.valueOf(entry.getKey().substring("combo_".length()));
            int qty = Integer.parseInt(entry.getValue());
            if (qty > 0) {
                comboQuantities.put(comboId, qty);
            }
        }

        Long promoId = (promotionId != null && !promotionId.isBlank()) ? Long.valueOf(promotionId) : null;

        String bookingCode = bookingConfirmService.confirmBooking(
                showtimeId, seatCodes, comboQuantities, paymentMethod, promoId, discountAmount);

        model.addAttribute("bookingCode", bookingCode);
        return "redirect:/";
    }
}