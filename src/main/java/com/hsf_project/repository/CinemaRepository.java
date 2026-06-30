package com.hsf_project.repository;

import com.hsf_project.entity.Cinema;
import com.hsf_project.entity.CinemaRoom;
import com.hsf_project.entity.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;


public interface CinemaRepository extends JpaRepository<Cinema,Integer> {


    @Query("""
        SELECT DISTINCT c
        FROM Cinema c
        JOIN FETCH c.cinemaRooms cr
        JOIN FETCH cr.showTimes st
        WHERE c.city.id = :cityId
        AND FUNCTION('DATE', st.startTime) = :selectDate
        AND c.isDeleted = false
        AND cr.isDeleted = false
        AND st.isDeleted = false
    """)
    List<Cinema> findCinemaSchedule(
            @Param("cityId") Integer cityId,
            @Param("selectDate") LocalDate selectDate
    );

    List<Cinema> findCinemaByCity(City city);

    List<Cinema> findCinemaByCity_Id(Integer cityId);
}
