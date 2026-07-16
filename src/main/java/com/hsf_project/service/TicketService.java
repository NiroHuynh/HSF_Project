package com.hsf_project.service;


import com.hsf_project.dto.response.BookingRowDTO;

import java.util.List;

public interface TicketService {

    // Dùng cho Staff — chỉ lấy booking thuộc cinema của staff đó
    List<BookingRowDTO> getBookingsByCinema(Integer cinemaId);
}