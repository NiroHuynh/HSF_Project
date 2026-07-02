package com.hsf_project.dto.response;

import java.util.ArrayList;
import java.util.List;

public class RoomScheduleResponse {

    private Integer roomId;

    private String roomName;

    private String roomType;

    private List<ShowTimeScheduleResponse> showTimes = new ArrayList<>();

    public RoomScheduleResponse(Integer roomId, String roomName, String roomType, List<ShowTimeScheduleResponse> showTimes) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.roomType = roomType;
        this.showTimes = showTimes;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public RoomScheduleResponse() {
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

    public List<ShowTimeScheduleResponse> getShowTimes() {
        return showTimes;
    }

    public void setShowTimes(List<ShowTimeScheduleResponse> showTimes) {
        this.showTimes = showTimes;
    }
}
