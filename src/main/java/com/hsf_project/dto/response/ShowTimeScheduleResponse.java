package com.hsf_project.dto.response;

import java.time.LocalDateTime;

public class ShowTimeScheduleResponse {

    private Long showTimeId;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    public ShowTimeScheduleResponse() {
    }

    public ShowTimeScheduleResponse(Long showTimeId, LocalDateTime startTime, LocalDateTime endTime) {
        this.showTimeId = showTimeId;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public ShowTimeScheduleResponse(Long showTimeId) {
        this.showTimeId = showTimeId;
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