package com.hsf_project.schedule;

import com.hsf_project.entity.Booking;
import com.hsf_project.entity.BookingStatus;
import com.hsf_project.entity.Ticket;
import com.hsf_project.repository.BookingRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class BookingCleanScheduler {

    private static final Logger log = LoggerFactory.getLogger(BookingCleanScheduler.class);

    /**
     * Hủy đơn muộn hơn hạn giữ ghế 5 phút: ghế đã tự nhả ngay khi hết hạn
     * (existsBookedSeat chỉ tính PENDING còn hạn), nhưng đợi thêm để VNPay
     * kịp redirect về nếu user bấm trả tiền sát giờ — tránh hủy đơn đã trả tiền.
     */
    private static final int GRACE_MINUTES = 5;

    @Autowired
    private BookingRepository bookingRepository;

    // Cứ mỗi 15 giây quét hệ thống 1 lần để dọn dẹp đơn hết hạn
    @Scheduled(fixedRate = 15000)
    @Transactional
    public void releaseExpiredSeats() {
        LocalDateTime now = LocalDateTime.now();
        List<Booking> expiredBookings = bookingRepository
                .findByStatusAndExpiredAtBeforeAndIsDeletedFalse(
                        BookingStatus.PENDING.name(), now.minusMinutes(GRACE_MINUTES));

        for (Booking booking : expiredBookings) {
            booking.setStatus(BookingStatus.CANCELED.name());
            booking.setUpdatedAt(now);

            if (booking.getTickets() != null) {
                for (Ticket ticket : booking.getTickets()) {
                    ticket.setIsDeleted(true); // Soft-delete giải phóng ghế
                }
            }
            bookingRepository.save(booking);
            log.info("Đã hủy đơn hết hạn và giải phóng ghế cho mã: {}", booking.getBookingCode());
        }
    }
}
