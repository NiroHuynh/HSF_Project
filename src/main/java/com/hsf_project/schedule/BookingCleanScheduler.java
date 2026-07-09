package com.hsf_project.schedule;

import com.hsf_project.entity.Booking;
import com.hsf_project.entity.BookingStatus;
import com.hsf_project.entity.Ticket;
import com.hsf_project.entity.User;
import com.hsf_project.repository.BookingRepository;
import com.hsf_project.repository.TicketRepository;
import com.hsf_project.repository.auth.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class BookingCleanScheduler {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;


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
            booking.setNote("SYSTEM_TIMEOUT");

            if (booking.getTickets() != null) {
                for (Ticket ticket : booking.getTickets()) {
                    ticket.setIsDeleted(true); // Soft-delete giải phóng ghế
                }
                //Đóng dấu lưu hàng loạt danh sách vé đã chỉnh sửa xuống DB
                ticketRepository.saveAll(booking.getTickets());
            }
            bookingRepository.save(booking);
            System.out.println("[HỆ THỐNG] Đã hủy đơn và giải phóng ghế cho mã: " + booking.getBookingCode());

            User user = booking.getUser();
            if(user != null){
                LocalDateTime startOfDay = LocalDate.now().atStartOfDay();

                long violations = bookingRepository.countByUserIdAndIsDeletedFalseAndStatusAndNoteAndBookingDateAfter(user.getId(), BookingStatus.CANCELED.name(), "SYSTEM_TIMEOUT", startOfDay);

                if(violations >= 3 ){
                    //Lấy giờ chính xác của châu Á/Hồ Chí Minh rồi cộng 15 phút
                    java.time.ZonedDateTime VietNamTime = java.time.ZonedDateTime.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh"));
                    user.setLockBookingUntil(VietNamTime.toLocalDateTime().plusMinutes(15));
                    userRepository.save(user);
                    System.out.println("[PHẠT] User: " + user.getEmail() + " bùng 3 lần. Khóa đặt vé đến: " + user.getLockBookingUntil());

                }
            }
        }
    }
}
