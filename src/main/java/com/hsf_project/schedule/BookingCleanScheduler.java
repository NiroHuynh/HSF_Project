package com.hsf_project.schedule;

import com.hsf_project.entity.Booking;
import com.hsf_project.entity.BookingStatus;
import com.hsf_project.entity.Ticket;
import com.hsf_project.repository.BookingRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class BookingCleanScheduler {
    @Autowired
    private BookingRepository bookingRepository;

    // Cứ mỗi 15 giây quét hệ thống 1 lần để dọn dẹp đơn hết hạn
    @Scheduled(fixedRate = 15000)
    @Transactional
    public void releaseExpiredSeats() {
        LocalDateTime now = LocalDateTime.now();
        List<Booking> expiredBookings = bookingRepository
                .findByStatusAndExpiredAtBeforeAndIsDeletedFalse(BookingStatus.PENDING.name(), now);

        for (Booking booking : expiredBookings) {
            booking.setStatus(BookingStatus.CANCELED.name());
            booking.setUpdatedAt(now);

            if (booking.getTickets() != null) {
                for (Ticket ticket : booking.getTickets()) {
                    ticket.setIsDeleted(true); // Soft-delete giải phóng ghế
                }
            }
            bookingRepository.save(booking);
            System.out.println("[HỆ THỐNG] Đã hủy đơn và giải phóng ghế cho mã: " + booking.getBookingCode());
        }
    }
}
