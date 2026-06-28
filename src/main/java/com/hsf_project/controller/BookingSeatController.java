package com.hsf_project.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

/**
 * DEMO Controller — chỉ để TEST render trang chọn ghế trong browser.
 * Phần demo*() là dữ liệu giả; khi có SeatService/ShowtimeService thật,
 * thay các dòng addAttribute() bằng dữ liệu thật rồi xoá demo*() đi.
 */
@Controller
@RequestMapping("/booking")
public class BookingSeatController {

    @GetMapping("/seats")
    public String seatSelection(@RequestParam(required = false) Long showtimeId, Model model) {

        List<SeatRowVM> seatRows = demoSeatRows();

        int total = 0;
        int available = 0;
        for (SeatRowVM row : seatRows) {
            for (SeatVM seat : row.getSeats()) {
                total++;
                if ("AVAILABLE".equals(seat.getStatus())) available++;
            }
        }

        model.addAttribute("showtime", demoShowtimeHeader());
        model.addAttribute("seatRows", seatRows);
        model.addAttribute("availableSeatsCount", available);
        model.addAttribute("totalSeatsCount", total);

        // Đổi tên view khớp với chỗ bạn lưu file html, ví dụ "booking/seat-selection"
        return "bookingSeat";
    }

    // ===================== DEMO DATA (xoá khi có Service thật) =====================

    private ShowtimeHeaderVM demoShowtimeHeader() {
        return new ShowtimeHeaderVM(
                1L,
                "JOKER: FOLIE À DEUX",
                "C18",
                "CGV Vincom Nguyễn Chí Thanh",
                "Cinema 7",
                "20/05/2026 22:50",
                "21/05/2026 00:42"
        );
    }

    private List<SeatRowVM> demoSeatRows() {
        List<SeatRowVM> rows = new ArrayList<>();
        long idCounter = 1;

        // Ghế thường: A, B, C — 10 ghế/hàng, giá 75.000đ
        for (char rowChar : new char[]{'A', 'B', 'C'}) {
            idCounter = addRow(rows, rowChar, 10, "STANDARD", 75000, idCounter);
        }

        // VIP: D, E, F — 10 ghế/hàng, giá 110.000đ
        for (char rowChar : new char[]{'D', 'E', 'F'}) {
            idCounter = addRow(rows, rowChar, 10, "VIP", 110000, idCounter);
        }

        // Sweetbox: G — 8 ghế, giá 150.000đ
        idCounter = addRow(rows, 'G', 8, "SWEETBOX", 150000, idCounter);

        // Đánh dấu vài ghế demo là đã đặt (BOOKED)
        markBooked(rows, "B7");
        markBooked(rows, "B8");
        markBooked(rows, "D5");
        markBooked(rows, "F2");

        return rows;
    }

    private long addRow(List<SeatRowVM> rows, char rowChar, int seatCount, String type, int price, long idCounter) {
        List<SeatVM> seats = new ArrayList<>();
        for (int i = 1; i <= seatCount; i++) {
            String code = rowChar + String.valueOf(i);
            seats.add(new SeatVM(idCounter++, code, type, "AVAILABLE", price));
        }
        rows.add(new SeatRowVM(String.valueOf(rowChar), seats));
        return idCounter;
    }

    private void markBooked(List<SeatRowVM> rows, String code) {
        for (SeatRowVM row : rows) {
            for (SeatVM seat : row.getSeats()) {
                if (seat.getCode().equals(code)) {
                    seat.setStatus("BOOKED");
                }
            }
        }
    }

    // ===================== View Model (mirror của template, đổi sang DTO thật) =====================

    public static class ShowtimeHeaderVM {
        private final Long movieId;
        private final String movieTitle;
        private final String ageRating;
        private final String cinemaName;
        private final String roomName;
        private final String startTimeLabel;
        private final String endTimeLabel;

        public ShowtimeHeaderVM(Long movieId, String movieTitle, String ageRating, String cinemaName,
                                String roomName, String startTimeLabel, String endTimeLabel) {
            this.movieId = movieId;
            this.movieTitle = movieTitle;
            this.ageRating = ageRating;
            this.cinemaName = cinemaName;
            this.roomName = roomName;
            this.startTimeLabel = startTimeLabel;
            this.endTimeLabel = endTimeLabel;
        }

        public Long getMovieId() { return movieId; }
        public String getMovieTitle() { return movieTitle; }
        public String getAgeRating() { return ageRating; }
        public String getCinemaName() { return cinemaName; }
        public String getRoomName() { return roomName; }
        public String getStartTimeLabel() { return startTimeLabel; }
        public String getEndTimeLabel() { return endTimeLabel; }
    }

    public static class SeatRowVM {
        private final String rowLabel;
        private final List<SeatVM> seats;

        public SeatRowVM(String rowLabel, List<SeatVM> seats) {
            this.rowLabel = rowLabel;
            this.seats = seats;
        }

        public String getRowLabel() { return rowLabel; }
        public List<SeatVM> getSeats() { return seats; }
    }

    public static class SeatVM {
        private final Long id;
        private final String code;
        private final String type;
        private String status;
        private final int price;

        public SeatVM(Long id, String code, String type, String status, int price) {
            this.id = id;
            this.code = code;
            this.type = type;
            this.status = status;
            this.price = price;
        }

        public Long getId() { return id; }
        public String getCode() { return code; }
        public String getType() { return type; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public int getPrice() { return price; }
    }
}