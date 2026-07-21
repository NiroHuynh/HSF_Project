package com.hsf_project.service.impl;

import com.hsf_project.entity.Booking;
import com.hsf_project.entity.BookingCombo;
import com.hsf_project.entity.Combo;
import com.hsf_project.repository.BookingComboRepository;
import com.hsf_project.repository.BookingRepository;
import com.hsf_project.service.BookingComboService;
import com.hsf_project.service.ComboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class BookingComboServiceImpl implements BookingComboService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private BookingComboRepository bookingComboRepository;

    @Autowired
    private ComboService comboService;

    @Override
    public void saveBookingCombos(String bookingCode, Map<String, String> allParams) {
        // 1. Lấy và validate Booking
        Booking booking = bookingRepository.findByBookingCodeAndIsDeletedFalse(bookingCode)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng: " + bookingCode));

        if (booking.getExpiredAt() == null || LocalDateTime.now().isAfter(booking.getExpiredAt())) {
            throw new IllegalStateException("TIMEOUT"); // Throw Exception để Controller catch
        }

        // 2. Lọc các combo chọn
        Map<Long, Integer> comboQuantities = new LinkedHashMap<>();
        BigDecimal comboTotal = BigDecimal.ZERO;

        for (Map.Entry<String, String> entry : allParams.entrySet()) {
            if (!entry.getKey().startsWith("combo_")) {
                continue;
            }
            Long comboId = Long.valueOf(entry.getKey().substring("combo_".length()));
            int qty = Integer.parseInt(entry.getValue());

            if (qty > 0) {
                comboQuantities.put(comboId, qty);
                Combo combo = comboService.getById(comboId);
                comboTotal = comboTotal.add(combo.getPrice().multiply(BigDecimal.valueOf(qty)));
            }
        }

        // 3. Xóa combo cũ
        bookingComboRepository.deleteByBookingId(booking.getId());

        // 4. Lưu danh sách combo mới
        for (Map.Entry<Long, Integer> entry : comboQuantities.entrySet()) {
            Combo combo = comboService.getById(entry.getKey());
            BookingCombo line = new BookingCombo();
            line.setBooking(booking);
            line.setCombo(combo);
            line.setQuantity(entry.getValue());
            line.setUnitPrice(combo.getPrice());
            line.setTotalPrice(combo.getPrice().multiply(BigDecimal.valueOf(entry.getValue())));
            line.setCreatedAt(LocalDateTime.now());
            bookingComboRepository.save(line);
        }

        // 5. Tính lại tổng tiền
        BigDecimal seatTotal = booking.getTickets().stream()
                .map(t -> t.getTicketPrice().getPrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalAmount = seatTotal.add(comboTotal);
        BigDecimal discountAmount = booking.getDiscountAmount() != null ? booking.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal finalAmount = totalAmount.subtract(discountAmount);

        booking.setTotalAmount(totalAmount);
        booking.setFinalAmount(finalAmount);
        booking.setUpdatedAt(LocalDateTime.now());

        bookingRepository.save(booking);
    }
}
