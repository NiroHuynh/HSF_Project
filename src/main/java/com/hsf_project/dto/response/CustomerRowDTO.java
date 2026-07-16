package com.hsf_project.dto.response;

public class CustomerRowDTO {

    private String id;       // user_id dạng "KH-001"
    private String name;     // lastName + firstName
    private String email;
    private String phone;
    private String dateISO;  // "2026-07-09" — dùng cho JS date filter
    private String date;     // "09/07/2026" — hiển thị trên bảng
    private long   tickets;  // số booking tại chi nhánh này
    private String spend;    // "1,250,000 đ"

    public CustomerRowDTO() {}

    // Getters & Setters
    public String getId()                { return id; }
    public void   setId(String id)       { this.id = id; }

    public String getName()              { return name; }
    public void   setName(String name)   { this.name = name; }

    public String getEmail()               { return email; }
    public void   setEmail(String email)   { this.email = email; }

    public String getPhone()               { return phone; }
    public void   setPhone(String phone)   { this.phone = phone; }

    public String getDateISO()                  { return dateISO; }
    public void   setDateISO(String dateISO)    { this.dateISO = dateISO; }

    public String getDate()                { return date; }
    public void   setDate(String date)     { this.date = date; }

    public long getTickets()               { return tickets; }
    public void setTickets(long tickets)   { this.tickets = tickets; }

    public String getSpend()               { return spend; }
    public void   setSpend(String spend)   { this.spend = spend; }
}