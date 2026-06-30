package com.hsf_project.dto.response;

import java.security.PrivateKey;

public class SeatResponse {
    private Long id;
    private String code;
    private String type;
    private boolean booked;

    public SeatResponse(Long id) {
        this.id = id;
    }

    public String cssClass() {

        if (booked) {
            return "seat--booked";
        }

        return switch (type) {
            case "VIP" -> "seat--vip";
            case "SWEETBOX" -> "seat--sweetbox";
            default -> "seat--standard";
        };
    }

    public SeatResponse(Long id, String code, String type, boolean booked) {
        this.id = id;
        this.code = code;
        this.type = type;
        this.booked = booked;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isBooked() {
        return booked;
    }

    public void setBooked(boolean booked) {
        this.booked = booked;
    }
}
