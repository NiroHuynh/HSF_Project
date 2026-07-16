package com.hsf_project.dto.response;

/**
 * DTO cho mỗi dòng trong bảng suất chiếu.
 * Tên field khớp với JS để JS dùng trực tiếp (s.movie, s.room, s.format...).
 */
public class ShowTimeRowDTO {

    private Long   id;       // showtime id — dùng cho link edit/delete
    private String movie;    // movie title
    private String poster;   // posterUrl
    private String age;      // ageRating (T13, T16, T18, P...)
    private String room;     // room name
    private String format;   // roomType: "2D" | "3D" | "IMAX"
    private String start;    // "HH:mm"
    private String end;      // "HH:mm"
    private String dateISO;  // "yyyy-MM-dd"
    private int    booked;   // số ghế đã đặt
    private int    total;    // tổng số ghế phòng

    public ShowTimeRowDTO() {}

    public Long   getId()                  { return id; }
    public void   setId(Long id)           { this.id = id; }
    public String getMovie()               { return movie; }
    public void   setMovie(String movie)   { this.movie = movie; }
    public String getPoster()              { return poster; }
    public void   setPoster(String poster) { this.poster = poster; }
    public String getAge()                 { return age; }
    public void   setAge(String age)       { this.age = age; }
    public String getRoom()                { return room; }
    public void   setRoom(String room)     { this.room = room; }
    public String getFormat()                  { return format; }
    public void   setFormat(String format)     { this.format = format; }
    public String getStart()               { return start; }
    public void   setStart(String start)   { this.start = start; }
    public String getEnd()                 { return end; }
    public void   setEnd(String end)       { this.end = end; }
    public String getDateISO()                  { return dateISO; }
    public void   setDateISO(String dateISO)    { this.dateISO = dateISO; }
    public int    getBooked()              { return booked; }
    public void   setBooked(int booked)    { this.booked = booked; }
    public int    getTotal()               { return total; }
    public void   setTotal(int total)      { this.total = total; }
}