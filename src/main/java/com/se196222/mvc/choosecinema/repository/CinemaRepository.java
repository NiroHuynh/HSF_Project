package com.se196222.mvc.choosecinema.repository;

import com.se196222.mvc.choosecinema.entity.Cinema;
import com.se196222.mvc.choosecinema.entity.CinemaRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CinemaRepository extends JpaRepository<Cinema, Integer> {
    // All active cities (not soft-deleted)
    List<Cinema> findByCityCityIdAndIsDeletedFalseOrderByName(Integer cityId);
}
