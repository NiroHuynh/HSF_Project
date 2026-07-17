package com.hsf_project.repository;

import com.hsf_project.entity.BookingCombo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingComboRepository extends JpaRepository<BookingCombo, Long> {

    /** Xóa toàn bộ combo cũ của một booking — dùng khi user quay lại sửa lựa chọn combo. */
    @Modifying
    @Query("DELETE FROM BookingCombo bc WHERE bc.booking.id = :bookingId")
    void deleteByBookingId(@Param("bookingId") Long bookingId);
}
