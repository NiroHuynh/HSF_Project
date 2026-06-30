package com.hsf_project.repository;

import com.hsf_project.entity.ShowTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ShowTimeRepository extends JpaRepository<ShowTime, Long> {

    @Query("SELECT st FROM ShowTime st " +
            "JOIN FETCH st.room r " +
            "JOIN FETCH r.cinema c " +
            "JOIN FETCH st.movie m " +
            "WHERE st.id = :id")
    Optional<ShowTime> findDetailById(Long id);
}