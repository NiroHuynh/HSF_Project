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

@Controller
@RequestMapping("/booking")
public class BookingConfirmController {

    @Autowired
    private BookingConfirmService bookingConfirmService;

    @Autowired
    private VnPayService vnPayService;

    /**
     * Bước cuối của luồng đặt vé (initiate → combo → payment → confirm):
     * booking đã tồn tại từ bước chọn ghế, ở đây chỉ chốt phương thức thanh toán
     * và mã khuyến mãi (server tự validate lại) rồi chuyển sang VNPay.
     */
    @PostMapping("/confirm")
    public String confirmBooking(
            @RequestParam String bookingCode,
            @RequestParam Long paymentMethodId,
            @RequestParam(required = false) String promoCode,
            HttpSession session,
            HttpServletRequest request) {

        // AuthInterceptor đã chặn /booking/** khi chưa đăng nhập nên user luôn tồn tại.
        User currentUser = (User) session.getAttribute("ttdn");

        ConfirmResult result = bookingConfirmService.preparePayment(
                bookingCode, currentUser.getId(), paymentMethodId, promoCode);

        // Voucher giảm 100%: không cần qua VNPay, chốt thành công luôn
        if (result.finalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            bookingConfirmService.finalizeBooking(result.bookingCode(), true, null, null, "FREE_BY_PROMOTION");
            return "redirect:/booking/result?code=" + result.bookingCode();
        }

        String paymentUrl = vnPayService.buildPaymentUrl(
                result.bookingCode(), result.finalAmount(), result.bankCode(),
                request.getRemoteAddr(), result.expiresAt());
        return "redirect:" + paymentUrl;
    }
}
