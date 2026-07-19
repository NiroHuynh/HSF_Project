package com.hsf_project.repository;

import com.hsf_project.entity.CinemaRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CinemaRoomRepository extends JpaRepository<CinemaRoom, Integer> {

    /**
     * Lấy danh sách phòng chiếu của chi nhánh — dùng cho dropdown khi tạo suất chiếu.
     * Chỉ lấy phòng chưa bị xóa, sắp xếp theo tên.
     */
    List<CinemaRoom> findByCinemaIdAndIsDeletedFalseOrderByNameAsc(Integer cinemaId);
    List<CinemaRoom> findByIsDeletedFalseOrderByCinemaNameAscNameAsc();
    List<CinemaRoom> findByRoomTypeAndIsDeletedFalse(String roomType);
}
