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
 * BƯỚC 2 trong luồng Đặt vé: Chọn ghế. Dựng lại từ đầu (bản cũ đang lỗi).
 *
 * Luồng dữ liệu (theo đúng quy ước đã thống nhất ở các bước Combo/Thanh toán):
 * - Trang TRƯỚC trang này là "bookingContext" (chọn phim/rạp/giờ chiếu).
 *   Mình CHƯA có code/route thật của trang đó, nên đang GIẢ ĐỊNH nó chỉ
 *   truyền duy nhất showtimeId qua query param sang đây (giống cách
 *   ComboController/PaymentController đang nhận showtimeId). Nếu
 *   bookingContext của bạn truyền thêm/khác param, báo lại để mình sửa.
 * - Trang SAU trang này là Combo (booking/combo). Khi bấm "Tiếp tục",
 *   form submit showtimeId + seatIds (vd "A1,A2") + seatTotal sang đó —
 *   khớp 100% với @RequestParam mà ComboController đang nhận.
 * - Nút "Quay về" mình tạm dùng history.back() (JS) vì chưa biết route
 *   thật của trang bookingContext.
 *
 * Toàn bộ thông tin phim/rạp/phòng/ghế/giá vẫn là MOCK DATA (demo nhanh).
 * Khi nối DB thật:
 *   - showtime  -> query show_time JOIN movie JOIN cinema_room JOIN cinema theo showtimeId
 *   - danh sách ghế -> query bảng seat theo room_id (is_active=1, is_deleted=0)
 *   - ghế đã bán -> query ticket theo showtime_id (status PAID/PENDING)
 *   - giá ghế  -> query ticket_price theo seat_type/screen_format/day_type/time_slot
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

        ShowTime showtime = showTimeService.getShowTimeById(showtimeId);
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