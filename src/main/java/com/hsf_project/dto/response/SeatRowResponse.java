package com.hsf_project.dto.response;

import com.hsf_project.entity.Seat;

import java.util.List;

public class SeatRowResponse {
    private  String label;
    private List<SeatResponse> seats;

    public SeatRowResponse(String label) {
        this.label = label;
    }

    public SeatRowResponse(String label, List<SeatResponse> seats) {
        this.label = label;
        this.seats = seats;
    }

    public SeatRowResponse() {
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<SeatResponse> getSeats() {
        return seats;
    }

    public void setSeats(List<SeatResponse> seats) {
        this.seats = seats;
    }
}
