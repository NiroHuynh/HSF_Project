package com.hsf_project.dto.response;

import java.util.List;

/**
 * DTO dùng cho trang manager/tickets.
 * Tên field khớp với property trong JS array của tickets.html
 * để không phải sửa lại JS logic (renderTable, applyFilters...).
 */
public class BookingRowDTO {

    /** Mã booking — hiển thị cột "Mã vé" */
    private String id;

    /** Họ + Tên khách hàng */
    private String customer;

    /** Tên phim */
    private String movie;

    private String posterUrl;

    /** Tên phòng + định dạng — ví dụ: "Screen 04 · IMAX" */
    private String room;

    /** Giờ chiếu — ví dụ: "19:30 – 21:45" */
    private String time;

    /** Ngày chiếu định dạng hiển thị — ví dụ: "20/05/2026" */
    private String date;

    /** Ngày chiếu định dạng ISO — ví dụ: "2026-05-20" (dùng cho JS date filter) */
    private String dateISO;

    /** Danh sách mã ghế — ví dụ: ["J12", "J13"] */
    private List<String> seats;

    /** Tổng tiền đã format — ví dụ: "542,000 đ" */
    private String total;

    /**
     * Trạng thái booking theo giá trị DB thực:
     * PENDING | PAID | CANCELLED | COMPLETED
     */
    private String status;

    // ── Constructors ──────────────────────────────────────────────────────────

    public BookingRowDTO() {}

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public String getId()                  { return id; }
    public void   setId(String id)         { this.id = id; }

    public String getCustomer()                    { return customer; }
    public void   setCustomer(String customer)     { this.customer = customer; }

    public String getMovie()               { return movie; }
    public void   setMovie(String movie)   { this.movie = movie; }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public String getRoom()                { return room; }
    public void   setRoom(String room)     { this.room = room; }

    public String getTime()                { return time; }
    public void   setTime(String time)     { this.time = time; }

    public String getDate()                { return date; }
    public void   setDate(String date)     { this.date = date; }

    public String getDateISO()                  { return dateISO; }
    public void   setDateISO(String dateISO)    { this.dateISO = dateISO; }

    public List<String> getSeats()                  { return seats; }
    public void  setSeats(List<String> seats){ this.seats = seats; }

    public String getTotal()               { return total; }
    public void   setTotal(String total)   { this.total = total; }

    public String getStatus()                  { return status; }
    public void   setStatus(String status)     { this.status = status; }
}