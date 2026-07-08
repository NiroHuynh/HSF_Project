package com.hsf_project.controller;

import com.hsf_project.dto.response.SeatRowResponse;
import com.hsf_project.entity.ShowTime;
import com.hsf_project.entity.TicketPrice;
import com.hsf_project.service.SeatService;
import com.hsf_project.service.ShowTimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * BƯỚC 2 trong luồng Đặt vé: Chọn ghế (dữ liệu thật từ DB).
 * Trang trước: bookingContext (chọn phim/rạp/giờ chiếu) truyền showtimeId sang.
 * Khi bấm "Tiếp tục", form POST showtimeId + seatIds (vd "A1,A2") sang
 * /booking/initiate để tạo booking PENDING giữ ghế, rồi chuyển tiếp sang bước Combo.
 */
@Controller
@RequestMapping("/booking")
public class BookingSeatController {

    @Autowired
    private ShowTimeService showTimeService;

    @Autowired
    private SeatService seatService;

    @GetMapping("/seats")
    public String showSeatPage(@RequestParam Long showtimeId, Model model) {

        ShowTime showtime = showTimeService.getById(showtimeId);
        List<SeatRowResponse> rows = seatService.getSeatMap(showtime.getRoom().getId(),showtimeId);
        int totalSeats = showtime.getRoom().getTotalSeats();
        int bookedCount = rows.stream()
                .flatMap(r -> r.getSeats().stream())
                .filter(r -> r.isBooked())
                .toList()
                .size();
        int availableSeats = totalSeats - bookedCount;

        List<TicketPrice> ticketPrices = showtime.getRoom().getTicketPrices();

        Map<String, BigDecimal> seatPriceJson = ticketPrices.stream()
                                                    .collect(Collectors.toMap(
                                                            TicketPrice::getSeatType,
                                                            TicketPrice::getPrice
                                                    ));

        model.addAttribute("showtimeId", showtimeId);
        model.addAttribute("showtime", showtime);
        model.addAttribute("rows", rows);
        model.addAttribute("totalSeats", totalSeats);
        model.addAttribute("availableSeats", availableSeats);
        model.addAttribute("seatPriceJson", seatPriceJson);

        return "bookingSeat";
    }
}