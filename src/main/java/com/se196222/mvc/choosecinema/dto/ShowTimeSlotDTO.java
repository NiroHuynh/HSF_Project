package com.se196222.mvc.choosecinema.dto;

public class ShowTimeSlotDTO {
    private Integer showtimeId;
    private String  timeLabel;    // "19:45"
    private boolean almostFull;   // hint: last slot of the day

    public ShowTimeSlotDTO() {}
    public ShowTimeSlotDTO(Integer showtimeId, String timeLabel, boolean almostFull) {
        this.showtimeId  = showtimeId;
        this.timeLabel   = timeLabel;
        this.almostFull  = almostFull;
    }

    public Integer getShowtimeId() { return showtimeId; }
    public void setShowtimeId(Integer showtimeId) { this.showtimeId = showtimeId; }

    public String getTimeLabel() { return timeLabel; }
    public void setTimeLabel(String timeLabel) { this.timeLabel = timeLabel; }

    public boolean isAlmostFull() { return almostFull; }
    public void setAlmostFull(boolean almostFull) { this.almostFull = almostFull; }
}
