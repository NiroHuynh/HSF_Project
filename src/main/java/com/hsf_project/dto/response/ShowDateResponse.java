package com.hsf_project.dto.response;

public class ShowDateResponse {
    private final String isoDate;
    private final int dayOfMonth;
    private final String month;
    private final String weekdayLabel;
    private final boolean selected;

    public ShowDateResponse(String isoDate, int dayOfMonth, String month, String weekdayLabel, boolean selected) {
        this.isoDate = isoDate;
        this.dayOfMonth = dayOfMonth;
        this.month = month;
        this.weekdayLabel = weekdayLabel;
        this.selected = selected;
    }

    public String getIsoDate() { return isoDate; }
    public int getDayOfMonth() { return dayOfMonth; }
    public String getMonth() { return month; }
    public String getWeekdayLabel() { return weekdayLabel; }
    public boolean isSelected() { return selected; }
}