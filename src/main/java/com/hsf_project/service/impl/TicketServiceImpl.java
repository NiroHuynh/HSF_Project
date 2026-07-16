package com.hsf_project.service.impl;

import com.hsf_project.dto.response.BookingRowDTO;
import com.hsf_project.entity.Booking;
import com.hsf_project.entity.Ticket;
import com.hsf_project.repository.BookingRepository;
import com.hsf_project.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class TicketServiceImpl implements TicketService {

    @Autowired
    private BookingRepository bookingRepository;

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FMT  = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter ISO_FMT   = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    @Transactional(readOnly = true)
    public List<BookingRowDTO> getBookingsByCinema(Integer cinemaId) {
        return bookingRepository
                .findByCinemaIdAndIsDeletedFalse(cinemaId)
                .stream()
                .filter(b -> b.getTickets() != null && !b.getTickets().isEmpty())
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // ── Mapping ───────────────────────────────────────────────────────────────

    private BookingRowDTO toDto(Booking b) {
        BookingRowDTO dto = new BookingRowDTO();

        dto.setId(b.getBookingCode());

        String fullName = b.getUser().getLastName() + " " + b.getUser().getFirstName();
        dto.setCustomer(fullName);

        List<Ticket> tickets = b.getTickets();
        if (tickets != null && !tickets.isEmpty()) {

            var showtime = tickets.get(0).getShowtime();
            var movie    = showtime.getMovie();
            var room     = showtime.getRoom();

            dto.setMovie(movie.getTitle());
            dto.setPosterUrl(movie.getPosterUrl());
            dto.setRoom(room.getName());
            dto.setTime(
                    showtime.getStartTime().format(TIME_FMT)
                            + " – "
                            + showtime.getEndTime().format(TIME_FMT)
            );
            dto.setDate(showtime.getStartTime().format(DATE_FMT));
            dto.setDateISO(showtime.getStartTime().format(ISO_FMT));

            List<String> seatCodes = tickets.stream()
                    .map(t -> t.getSeat().getSeatCode())
                    .sorted()
                    .collect(Collectors.toList());
            dto.setSeats(seatCodes);

        } else {
            dto.setMovie("—");
            dto.setRoom("—");
            dto.setTime("—");
            dto.setDate("—");
            dto.setDateISO("");
            dto.setSeats(Collections.emptyList());
        }

        NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
        nf.setMaximumFractionDigits(0);
        dto.setTotal(nf.format(b.getFinalAmount()) + " đ");
        dto.setStatus(b.getStatus());

        return dto;
    }
}