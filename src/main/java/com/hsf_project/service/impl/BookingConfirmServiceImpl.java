package com.hsf_project.service.impl;

import com.hsf_project.entity.*;
import com.hsf_project.repository.*;
import com.hsf_project.service.BookingConfirmService;
import com.hsf_project.service.ComboService;
import com.hsf_project.service.PromotionService;
import com.hsf_project.service.ShowTimeService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
public class BookingConfirmServiceImpl implements BookingConfirmService {

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
    public String confirmBooking(Long showtimeId, List<String> seatCodes, Map<Long, Integer> comboQuantities, Long paymentMethodId, Long promotionId, BigDecimal discountAmount) {
        return "";
    }

//    @Override
//    @Transactional
//    public ConfirmResult confirmBooking(Long userId, Long showtimeId, List<String> seatCodes, Map<Long, Integer> comboQuantities,
//                                        Long paymentMethodId, Long promotionId,
//                                        BigDecimal discountAmount) {
//
//        LocalDateTime now = LocalDateTime.now();
//        ShowTime showtime = showTimeService.getById(showtimeId);
//        Integer roomId = showtime.getRoom().getId();
//
//        List<Seat> seats = new ArrayList<>();
//        List<TicketPrice> ticketPrices = new ArrayList<>();
//        BigDecimal seatTotal = BigDecimal.ZERO;
//
//        for (String seatCode : seatCodes) {
//            Seat seat = seatRepository.findByRoomIdAndSeatCodeAndIsDeletedFalse(roomId, seatCode)
//                    .orElseThrow(() -> new IllegalArgumentException("Ghế không tồn tại: " + seatCode));
//            TicketPrice ticketPrice = ticketPriceRepository
//                    .findByRoomIdAndSeatTypeAndIsDeletedFalse(roomId, seat.getType())
//                    .orElseThrow(() -> new IllegalArgumentException("Chưa có giá vé cho loại ghế: " + seat.getType()));
//
//            seats.add(seat);
//            ticketPrices.add(ticketPrice);
//            seatTotal = seatTotal.add(ticketPrice.getPrice());
//        }
//
//        List<Combo> combos = new ArrayList<>();
//        BigDecimal comboTotal = BigDecimal.ZERO;
//
//        for (Map.Entry<Long, Integer> entry : comboQuantities.entrySet()) {
//            Combo combo = comboService.getById(entry.getKey());
//            combos.add(combo);
//            comboTotal = comboTotal.add(combo.getPrice().multiply(BigDecimal.valueOf(entry.getValue())));
//        }
//
//        BigDecimal totalAmount = seatTotal.add(comboTotal);
//        BigDecimal finalAmount = totalAmount.subtract(discountAmount);
//
//        Booking booking = new Booking();
//        booking.setUser(entityManager.getReference(User.class, userId));
//        booking.setPromotion(promotionId != null ? entityManager.getReference(Promotion.class, promotionId) : null);
//        booking.setBookingCode(generateBookingCode());
//        booking.setBookingDate(now);
//        booking.setTotalAmount(totalAmount);
//        booking.setDiscountAmount(discountAmount);
//        booking.setFinalAmount(finalAmount);
//        // PENDING chờ kết quả VNPay; finalizeBooking sẽ chuyển sang PAID/CANCELLED
//        booking.setStatus("PENDING");
//        booking.setCreatedAt(now);
//        booking.setUpdatedAt(now);
//        booking.setIsDeleted(false);
//        booking = bookingRepository.save(booking);
//
//        for (int i = 0; i < seats.size(); i++) {
//            Ticket ticket = new Ticket();
//            ticket.setBooking(booking);
//            ticket.setShowtime(showtime);
//            ticket.setSeat(seats.get(i));
//            ticket.setTicketPrice(ticketPrices.get(i));
//            ticket.setStatus("PENDING");
//            ticket.setBookedAt(now);
//            ticket.setPaidAt(null);
//            ticket.setIsDeleted(false);
//            ticketRepository.save(ticket);
//        }
//
//        int idx = 0;
//        for (Map.Entry<Long, Integer> entry : comboQuantities.entrySet()) {
//            Combo combo = combos.get(idx++);
//            BookingCombo line = new BookingCombo();
//            line.setBooking(booking);
//            line.setCombo(combo);
//            line.setQuantity(entry.getValue());
//            line.setUnitPrice(combo.getPrice());
//            line.setTotalPrice(combo.getPrice().multiply(BigDecimal.valueOf(entry.getValue())));
//            line.setCreatedAt(now);
//            bookingComboRepository.save(line);
//        }
//
//        PaymentMethod paymentMethod = entityManager.getReference(PaymentMethod.class, paymentMethodId);
//
//        Payment payment = new Payment();
//        payment.setBooking(booking);
//        payment.setPaymentMethod(paymentMethod);
//        payment.setAmount(finalAmount);
//        payment.setPaymentTime(null);
//        payment.setPaymentStatus("PENDING");
//        payment.setCreatedAt(now);
//        paymentRepository.save(payment);
//
//        // Voucher chỉ được đánh dấu đã dùng khi thanh toán thành công (finalizeBooking)
//
//        return new ConfirmResult(booking.getBookingCode(), finalAmount, toVnpBankCode(paymentMethod));
//    }

    @Override
    @Transactional
    public ConfirmResult confirmBooking(Long userId, Long showtimeId, List<String> seatCodes, Map<Long, Integer> comboQuantities,
                                        Long paymentMethodId, Long promotionId,
                                        BigDecimal discountAmount) {

        LocalDateTime now = LocalDateTime.now();
        // Đồng bộ 15 phút giữ ghế với hạn thanh toán VNPay
        LocalDateTime expiredTime = now.plusMinutes(15);

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

        // Xử lý combo (nếu ban đầu chưa chọn thì map truyền vào sẽ trống, không sao cả)
        List<Combo> combos = new ArrayList<>();
        BigDecimal comboTotal = BigDecimal.ZERO;
        if (comboQuantities != null) {
            for (Map.Entry<Long, Integer> entry : comboQuantities.entrySet()) {
                Combo combo = comboService.getById(entry.getKey());
                combos.add(combo);
                comboTotal = comboTotal.add(combo.getPrice().multiply(BigDecimal.valueOf(entry.getValue())));
            }
        }

        BigDecimal totalAmount = seatTotal.add(comboTotal);
        BigDecimal finalAmount = totalAmount.subtract(discountAmount);

        //TẠO BOOKING TẠM THỜI (PENDING)
        Booking booking = new Booking();
        booking.setUser(entityManager.getReference(User.class, userId));
        booking.setPromotion(promotionId != null ? entityManager.getReference(Promotion.class, promotionId) : null);
        booking.setBookingCode(generateBookingCode());
        booking.setBookingDate(now);
        booking.setTotalAmount(totalAmount);
        booking.setDiscountAmount(discountAmount);
        booking.setFinalAmount(finalAmount);
        booking.setStatus(BookingStatus.PENDING.name());
        booking.setCreatedAt(now);
        booking.setUpdatedAt(now);
        booking.setExpiredAt(expiredTime); //Lưu thời gian hết hạn vào DB
        booking.setIsDeleted(false);
        booking = bookingRepository.save(booking);

        //KHÓA GHẾ TẠM THỜI (Tạo ticket PENDING)
        for (int i = 0; i < seats.size(); i++) {
            Ticket ticket = new Ticket();
            ticket.setBooking(booking);
            ticket.setShowtime(showtime);
            ticket.setSeat(seats.get(i));
            ticket.setTicketPrice(ticketPrices.get(i));
            ticket.setStatus(TicketStatus.PENDING.name());
            ticket.setBookedAt(now);
            ticket.setPaidAt(null);
            ticket.setIsDeleted(false);
            ticketRepository.save(ticket);
        }

        //SỬA ĐOẠN LƯU CHI TIẾT COMBO (Thêm check combos.size() > 0)
        if (comboQuantities != null && !combos.isEmpty()) {
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
        }

        //SỬA ĐOẠN TẠO PAYMENT (Chỉ tạo nếu có paymentMethodId được truyền vào)
        String bankCode = null;
        if (paymentMethodId != null) {
            PaymentMethod paymentMethod = entityManager.getReference(PaymentMethod.class, paymentMethodId);

            Payment payment = new Payment();
            payment.setBooking(booking);
            payment.setPaymentMethod(paymentMethod);
            payment.setAmount(finalAmount);
            payment.setPaymentTime(null);
            payment.setPaymentStatus(BookingStatus.PENDING.name());
            payment.setCreatedAt(now);
            paymentRepository.save(payment);

            bankCode = toVnpBankCode(paymentMethod);
        }

        // Trả về kết quả bình thường, bankCode lúc này ở bước 1 sẽ là null (hoàn toàn hợp lệ)
        return new ConfirmResult(booking.getBookingCode(), finalAmount, bankCode);
    }

    @Override
    @Transactional
    public void finalizeBooking(String bookingCode, boolean success,
                                String transactionNo, String responseCode, String rawParams) {
        Booking booking = bookingRepository.findByBookingCodeAndIsDeletedFalse(bookingCode)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy booking: " + bookingCode));

        // Idempotent: user F5 lại Return URL thì booking đã được chốt, không xử lý lại
        if (!"PENDING".equals(booking.getStatus())) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        Payment payment = paymentRepository.findFirstByBookingIdOrderByIdDesc(booking.getId())
                .orElseThrow(() -> new IllegalStateException("Booking " + bookingCode + " không có bản ghi payment"));
        payment.setTransactionCode(transactionNo);
        payment.setGatewayResponse(rawParams);
        payment.setPaymentTime(now);

        if (success) {
            booking.setStatus("PAID");
            for (Ticket ticket : booking.getTickets()) {
                ticket.setStatus("PAID");
                ticket.setPaidAt(now);
            }
            payment.setPaymentStatus("SUCCESS");
            if (booking.getPromotion() != null) {
                promotionService.markUsed(booking.getPromotion().getId());
            }
        } else {
            booking.setStatus("CANCELLED");
            // Ràng buộc CK_ticket_status trong DB chỉ cho PAID/PENDING nên không set
            // ticket = CANCELLED được; soft-delete để giải phóng ghế
            // (existsBookedSeat lọc isDeleted = false).
            for (Ticket ticket : booking.getTickets()) {
                ticket.setIsDeleted(true);
            }
            payment.setPaymentStatus("FAILED");
        }

        booking.setUpdatedAt(now);
        bookingRepository.save(booking);
        paymentRepository.save(payment);
    }

    /**
     * Map phương thức user chọn trên trang thanh toán sang kênh VNPay (vnp_BankCode).
     * Mọi giao dịch đều đi qua VNPay sandbox; null = user tự chọn kênh trên trang VNPay.
     */
    private String toVnpBankCode(PaymentMethod method) {
        String name = method.getMethodName() == null ? "" : method.getMethodName();
        if (name.equalsIgnoreCase("The ATM Noi Dia")) {
            return "VNBANK";
        }
        if (name.equalsIgnoreCase("The Quoc Te")) {
            return "INTCARD";
        }
        if (name.equalsIgnoreCase("QR Code")) {
            return "VNPAYQR";
        }
        return null;
    }

    private String generateBookingCode() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmm"));
        int random = new Random().nextInt(9000) + 1000;
        return "BK" + timestamp + random;
    }
}