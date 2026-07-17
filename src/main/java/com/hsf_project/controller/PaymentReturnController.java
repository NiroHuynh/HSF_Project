package com.hsf_project.controller;

import com.hsf_project.entity.Booking;
import com.hsf_project.entity.enums.BookingStatus;
import com.hsf_project.entity.User;
import com.hsf_project.repository.BookingRepository;
import com.hsf_project.service.BookingConfirmService;
import com.hsf_project.service.VnPayService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
public class PaymentReturnController {

    @Autowired
    private VnPayService vnPayService;

    @Autowired
    private BookingConfirmService bookingConfirmService;

    @Autowired
    private BookingRepository bookingRepository;

    /**
     * VNPay redirect trình duyệt về đây sau khi user thanh toán/hủy.
     * KHÔNG nằm sau AuthInterceptor: phải xử lý được cả khi session hết hạn
     * trong lúc user ở trang VNPay (bookingCode nằm trong vnp_TxnRef).
     */
    @GetMapping("/payment/vnpay-return")
    public String vnpayReturn(@RequestParam Map<String, String> params, HttpServletRequest request) {
        if (!vnPayService.verifyReturn(params)) {
            return "redirect:/booking/result?status=invalid";
        }

        String bookingCode = params.get("vnp_TxnRef");
        // "00" = giao dịch thành công; "24" = user hủy; mã khác = thất bại
        boolean success = "00".equals(params.get("vnp_ResponseCode"));

        bookingConfirmService.finalizeBooking(bookingCode, success,
                params.get("vnp_TransactionNo"), params.get("vnp_ResponseCode"),
                request.getQueryString());

        return "redirect:/booking/result?code=" + bookingCode;
    }

    /**
     * Trang kết quả đặt vé (nằm sau AuthInterceptor vì thuộc /booking/**).
     * Chỉ cho xem booking của chính user đang đăng nhập.
     */
    @GetMapping("/booking/result")
    @Transactional(readOnly = true)
    public String bookingResult(@RequestParam(required = false) String code,
                                @RequestParam(required = false) String status,
                                HttpSession session,
                                Model model) {
        if ("invalid".equals(status) || code == null || code.isBlank()) {
            model.addAttribute("invalid", true);
            return "bookingResult";
        }

        User currentUser = (User) session.getAttribute("ttdn");
        Booking booking = bookingRepository.findByBookingCodeAndIsDeletedFalse(code).orElse(null);

        if (booking == null || !booking.getUser().getId().equals(currentUser.getId())) {
            model.addAttribute("invalid", true);
            return "bookingResult";
        }

        model.addAttribute("booking", booking);
        model.addAttribute("success", BookingStatus.CONFIRMED.name().equals(booking.getStatus()));
        if (!booking.getTickets().isEmpty()) {
            model.addAttribute("showtime", booking.getTickets().get(0).getShowtime());
        }
        return "bookingResult";
    }
}
