package com.hsf_project.service;

import com.hsf_project.entity.*;
import com.hsf_project.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
public class BookingConfirmServiceImpl implements BookingConfirmService {

    // TODO: thay bằng userId thật khi có module đăng nhập (Spring Security/session).
    // Tạm hard-code user_id=8 (phong.huynh@gmail.com, role CUSTOMER) để test luồng booking.
    private static final Long HARDCODED_USER_ID = 8L;

    @Autowired
    private ShowTimeService showTimeService;

    @Autowired
    private ComboService comboService;

    @Autowired
    private PromotionService promotionService;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private TicketPriceRepository ticketPriceRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private BookingComboRepository bookingComboRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public String confirmBooking(Long showtimeId, List<String> seatCodes, Map<Long, Integer> comboQuantities,
                                 Long paymentMethodId, Long promotionId,
                                 BigDecimal discountAmount, BigDecimal serviceFee) {

        LocalDateTime now = LocalDateTime.now();
        ShowTime showtime = showTimeService.getById(showtimeId);
        Integer roomId = showtime.getRoom().getId();

        List<Seat> seats = new ArrayList<>();
        List<TicketPrice> ticketPrices = new ArrayList<>();
        BigDecimal seatTotal = BigDecimal.ZERO;

        for (String seatCode : seatCodes) {
            Seat seat = seatRepository.findByRoomIdAndSeatCodeAndIsDeletedFalse(roomId, seatCode)
                    .orElseThrow(() -> new IllegalArgumentException("Ghế không tồn tại: " + seatCode));
            TicketPrice ticketPrice = ticketPriceRepository
                    .findByRoomIdAndSeatTypeAndIsDeletedFalse(roomId, seat.getType())
                    .orElseThrow(() -> new IllegalArgumentException("Chưa có giá vé cho loại ghế: " + seat.getType()));

            seats.add(seat);
            ticketPrices.add(ticketPrice);
            seatTotal = seatTotal.add(ticketPrice.getPrice());
        }

        List<Combo> combos = new ArrayList<>();
        BigDecimal comboTotal = BigDecimal.ZERO;

        for (Map.Entry<Long, Integer> entry : comboQuantities.entrySet()) {
            Combo combo = comboService.getById(entry.getKey());
            combos.add(combo);
            comboTotal = comboTotal.add(combo.getPrice().multiply(BigDecimal.valueOf(entry.getValue())));
        }

        BigDecimal totalAmount = seatTotal.add(comboTotal);
        BigDecimal finalAmount = totalAmount.subtract(discountAmount).add(serviceFee);

        Booking booking = new Booking();
        booking.setUser(entityManager.getReference(User.class, HARDCODED_USER_ID));
        booking.setPromotion(promotionId != null ? entityManager.getReference(Promotion.class, promotionId) : null);
        booking.setBookingCode(generateBookingCode());
        booking.setBookingDate(now);
        booking.setTotalAmount(totalAmount);
        booking.setDiscountAmount(discountAmount);
        booking.setFinalAmount(finalAmount);
        booking.setStatus("PAID");
        booking.setCreatedAt(now);
        booking.setUpdatedAt(now);
        booking.setIsDeleted(false);
        booking = bookingRepository.save(booking);

        for (int i = 0; i < seats.size(); i++) {
            Ticket ticket = new Ticket();
            ticket.setBooking(booking);
            ticket.setShowtime(showtime);
            ticket.setSeat(seats.get(i));
            ticket.setTicketPrice(ticketPrices.get(i));
            ticket.setStatus("PAID");
            ticket.setBookedAt(now);
            ticket.setPaidAt(now);
            ticket.setIsDeleted(false);
            ticketRepository.save(ticket);
        }

        int idx = 0;
        for (Map.Entry<Long, Integer> entry : comboQuantities.entrySet()) {
            Combo combo = combos.get(idx++);
            BookingCombo line = new BookingCombo();
            line.setBooking(booking);
            line.setCombo(combo);
            line.setQuantity(entry.getValue());
            line.setUnitPrice(combo.getPrice());
            line.setTotalPrice(combo.getPrice().multiply(BigDecimal.valueOf(entry.getValue())));
            line.setCreatedAt(now);
            bookingComboRepository.save(line);
        }

        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setPaymentMethod(entityManager.getReference(PaymentMethod.class, paymentMethodId));
        payment.setAmount(finalAmount);
        payment.setPaymentTime(now);
        payment.setPaymentStatus("SUCCESS");
        payment.setCreatedAt(now);
        paymentRepository.save(payment);

        if (promotionId != null) {
            promotionService.markUsed(promotionId);
        }

        return booking.getBookingCode();
    }

    private String generateBookingCode() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmm"));
        int random = new Random().nextInt(9000) + 1000;
        return "BK" + timestamp + random;
    }
}