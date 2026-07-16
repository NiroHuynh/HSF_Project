package com.hsf_project.service;

import com.hsf_project.dto.response.ShowTimeRowDTO;
import com.hsf_project.entity.CinemaRoom;
import com.hsf_project.entity.Movie;
import com.hsf_project.entity.ShowTime;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface ManagerShowTimeService {

    // ── Danh sách ─────────────────────────────────────────────────────────────
    List<ShowTimeRowDTO> getShowtimesByDate(Integer cinemaId, LocalDate date);

    // ── Thêm mới ──────────────────────────────────────────────────────────────
    List<Movie>      getAvailableMovies();
    List<CinemaRoom> getRoomsForCinema(Integer cinemaId);
    String createShowtime(Integer cinemaId, Integer movieId, Integer roomId, LocalDateTime startTime);

    // ── Chỉnh sửa ─────────────────────────────────────────────────────────────

    /**
     * Lấy showtime để pre-fill form edit.
     * @return ShowTime nếu tồn tại và thuộc cinema, null nếu không tìm thấy
     */
    ShowTime getShowtimeForEdit(Long showtimeId, Integer cinemaId);

    /**
     * Validate và cập nhật suất chiếu.
     * Điều kiện giống thêm mới, nhưng check conflict loại trừ chính suất chiếu đang edit.
     * @return null nếu thành công, chuỗi lỗi nếu thất bại
     */
    String updateShowtime(Long showtimeId, Integer cinemaId,
                          Integer movieId, Integer roomId, LocalDateTime startTime);

    // ── Xóa ───────────────────────────────────────────────────────────────────

    /**
     * Soft delete suất chiếu (set isDeleted = true).
     * Không cho xóa nếu đã có vé được đặt.
     * @return null nếu thành công, chuỗi lỗi nếu thất bại
     */
    String deleteShowtime(Long showtimeId, Integer cinemaId);
}