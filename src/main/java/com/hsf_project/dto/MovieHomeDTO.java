package com.hsf_project.dto;

import com.hsf_project.entity.Movie;
import java.util.List;

/**
 * DTO bọc ngoài Entity Movie giúp bổ sung thuộc tính chuỗi thể loại (genresString)
 * và danh sách loại phòng chiếu (roomTypes) phục vụ riêng cho việc hiển thị lên trang chủ và trang phim.
 */
public class MovieHomeDTO {
    private Movie movie;
    private String genresString;
    private List<String> roomTypes;

    public MovieHomeDTO() {}

    public MovieHomeDTO(Movie movie, String genresString) {
        this.movie = movie;
        this.genresString = genresString;
        this.roomTypes = List.of("2D");
    }

    public MovieHomeDTO(Movie movie, String genresString, List<String> roomTypes) {
        this.movie = movie;
        this.genresString = genresString;
        this.roomTypes = roomTypes;
    }

    public Movie getMovie() { return movie; }
    public void setMovie(Movie movie) { this.movie = movie; }

    public String getGenresString() { return genresString; }
    public void setGenresString(String genresString) { this.genresString = genresString; }

    public List<String> getRoomTypes() { return roomTypes; }
    public void setRoomTypes(List<String> roomTypes) { this.roomTypes = roomTypes; }
}
