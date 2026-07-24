package com.hsf_project.entity.enums;

public enum AgeRating {
    P("P - Phim dành cho mọi lứa tuổi"),
    K("K - Dưới 13 tuổi xem cùng cha mẹ hoặc người giám hộ"),
    T13("T13 - Từ đủ 13 tuổi trở lên"),
    T16("T16 - Từ đủ 16 tuổi trở lên"),
    T18("T18 - Từ đủ 18 tuổi trở lên");

    private final String description;

    AgeRating(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}