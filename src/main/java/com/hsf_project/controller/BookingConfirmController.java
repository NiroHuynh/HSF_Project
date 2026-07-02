package com.hsf_project.controller;

import com.hsf_project.entity.User;
import com.hsf_project.service.BookingConfirmService;
import com.hsf_project.service.BookingConfirmService.ConfirmResult;
import com.hsf_project.service.VnPayService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
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

    @Autowired
    private VnPayService vnPayService;

    @PostMapping("/confirm")
    public String confirmBooking(
            @RequestParam Long showtimeId,
            @RequestParam String seatIds,
            @RequestParam BigDecimal discountAmount,
            @RequestParam(required = false) String promotionId,
            @RequestParam Long paymentMethod,
            @RequestParam Map<String, String> allParams,
            HttpSession session,
            HttpServletRequest request) {

        // AuthInterceptor đã chặn /booking/** khi chưa đăng nhập nên user luôn tồn tại.
        User currentUser = (User) session.getAttribute("ttdn");

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

        ConfirmResult result = bookingConfirmService.confirmBooking(
                currentUser.getId(), showtimeId, seatCodes, comboQuantities, paymentMethod, promoId, discountAmount);

        // Voucher giảm 100%: không cần qua VNPay, chốt thành công luôn
        if (result.finalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            bookingConfirmService.finalizeBooking(result.bookingCode(), true, null, null, "FREE_BY_PROMOTION");
            return "redirect:/booking/result?code=" + result.bookingCode();
        }

        String paymentUrl = vnPayService.buildPaymentUrl(
                result.bookingCode(), result.finalAmount(), result.bankCode(), request.getRemoteAddr());
        return "redirect:" + paymentUrl;
    }
}