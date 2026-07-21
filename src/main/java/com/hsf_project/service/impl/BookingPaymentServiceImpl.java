package com.hsf_project.service.impl;

import com.hsf_project.dto.response.PaymentPageData;
import com.hsf_project.entity.Booking;
import com.hsf_project.entity.PaymentMethod;
import com.hsf_project.entity.ShowTime;
import com.hsf_project.entity.Ticket;
import com.hsf_project.entity.enums.BookingStatus;
import com.hsf_project.repository.BookingRepository;
import com.hsf_project.repository.TicketRepository;
import com.hsf_project.service.BookingPaymentService;
import com.hsf_project.service.PaymentMethodService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingPaymentServiceImpl implements BookingPaymentService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private PaymentMethodService paymentMethodService;

    @Override
    public PaymentPageData getPaymentPageData(String bookingCode) {
        // 1. Tìm thông tin Booking
        Booking booking = bookingRepository.findByBookingCodeAndIsDeletedFalse(bookingCode)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy mã đơn hàng: " + bookingCode));

        // 2. Tính số giây đếm ngược còn lại
        long secondsLeft = booking.getExpiredAt() == null ? 0
                : Duration.between(LocalDateTime.now(), booking.getExpiredAt()).toSeconds();

        if (secondsLeft <= 0) {
            throw new IllegalStateException("TIMEOUT");
        }

        // 3. Lấy danh sách mã ghế
        List<String> selectedSeats = booking.getTickets().stream()
                .map(ticket -> ticket.getSeat().getSeatCode())
                .toList();

        // 4. Lấy danh sách Combo đã chọn
        List<PaymentPageData.SelectedComboDTO> selectedCombos = booking.getBookingCombos().stream()
                .map(bc -> new PaymentPageData.SelectedComboDTO(
                        bc.getCombo().getName(),
                        bc.getQuantity(),
                        bc.getTotalPrice()
                )).toList();

        // 5. Suất chiếu & Danh sách PTTT
        ShowTime showtime = booking.getTickets().get(0).getShowtime();
        List<PaymentMethod> paymentMethods = paymentMethodService.getActiveMethods();

        // 6. Đóng gói vào DTO và trả về
        return new PaymentPageData(
                bookingCode,
                secondsLeft,
                selectedSeats,
                selectedCombos,
                booking.getTotalAmount(),
                booking.getDiscountAmount() != null ? booking.getDiscountAmount() : BigDecimal.ZERO,
                booking.getFinalAmount(),
                paymentMethods,
                showtime
        );
    }

    @Transactional
    @Override
    public void cancelBooking(String bookingCode) {
        bookingRepository.findByBookingCodeAndIsDeletedFalse(bookingCode)
                .ifPresent(booking -> {
                    booking.setStatus(BookingStatus.CANCELED.name());
                    booking.setUpdatedAt(LocalDateTime.now());

                    if (booking.getTickets() != null) {
                        for (Ticket ticket : booking.getTickets()) {
                            ticket.setIsDeleted(true); // Soft-delete giải phóng ghế
                        }
                        ticketRepository.saveAll(booking.getTickets());
                    }
                    bookingRepository.save(booking);
                });
    }
}
