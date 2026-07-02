package com.hsf_project.dto;

import com.hsf_project.entity.Movie;

/**
 * DTO bọc ngoài Entity Movie giúp bổ sung thuộc tính chuỗi thể loại (genresString)
 * phục vụ riêng cho việc hiển thị gọn đẹp lên trang chủ.
 */
public class MovieHomeDTO {
    private Movie movie;
    private String genresString;

    public MovieHomeDTO(Movie movie, String genresString) {
        this.movie = movie;
        this.genresString = genresString;
    }

    // Các Getter để Thymeleaf truy xuất qua cú pháp mã động
    public Movie getMovie() { return movie; }
    public String getGenresString() { return genresString; }
}
