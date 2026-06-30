package com.hsf_project.repository;

import com.hsf_project.entity.MovieGenre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovieGenreRepository extends JpaRepository<MovieGenre, Integer> {
    /**
     * Lấy danh sách liên kết thể loại của một bộ phim cụ thể (Dùng để hiển thị tag tên thể loại)
     */
    List<MovieGenre> findByMovieId(Integer movieId);
}
