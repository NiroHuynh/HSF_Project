package com.hsf_project.dto.response;



import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CinemaScheduleResponse {

    private Integer cinemaId;

    private String cinemaName;

    private String address;

    private List<RoomScheduleResponse> rooms = new ArrayList<>();

    public CinemaScheduleResponse() {
    }

    public CinemaScheduleResponse(Integer cinemaId, String cinemaName, String address, List<RoomScheduleResponse> rooms) {
        this.cinemaId = cinemaId;
        this.cinemaName = cinemaName;
        this.address = address;
        this.rooms = rooms;
    }

    public Integer getCinemaId() {
        return cinemaId;
    }

    public void setCinemaId(Integer cinemaId) {
        this.cinemaId = cinemaId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCinemaName() {
        return cinemaName;
    }

    public void setCinemaName(String cinemaName) {
        this.cinemaName = cinemaName;
    }

    public List<RoomScheduleResponse> getRooms() {
        return rooms;
    }

    public void setRooms(List<RoomScheduleResponse> rooms) {
        this.rooms = rooms;
    }
}
