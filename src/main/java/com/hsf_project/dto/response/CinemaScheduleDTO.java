package com.hsf_project.dto.response;

import java.time.LocalDateTime;

public class CinemaScheduleDTO{
    private Integer cinemaId;
    private String address;
    private String cinemaName;

    private Integer roomId;

    private String roomName;
    private String roomType;

    private Long showTimeId;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public CinemaScheduleDTO(Integer cinemaId, String address, String cinemaName, Integer roomId, String roomName, String roomType, Long showTimeId, LocalDateTime startTime, LocalDateTime endTime) {
        this.cinemaId = cinemaId;
        this.address = address;
        this.cinemaName = cinemaName;
        this.roomId = roomId;
        this.roomName = roomName;
        this.roomType = roomType;
        this.showTimeId = showTimeId;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public CinemaScheduleDTO() {
    }

    public Integer getCinemaId() {
        return cinemaId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setCinemaId(Integer cinemaId) {
        this.cinemaId = cinemaId;
    }

    public String getCinemaName() {
        return cinemaName;
    }

    public void setCinemaName(String cinemaName) {
        this.cinemaName = cinemaName;
    }

    public Integer getRoomId() {
        return roomId;
    }

    public void setRoomId(Integer roomId) {
        this.roomId = roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public Long getShowTimeId() {
        return showTimeId;
    }

    public void setShowTimeId(Long showTimeId) {
        this.showTimeId = showTimeId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
}
