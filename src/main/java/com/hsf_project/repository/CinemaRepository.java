package com.hsf_project.repository;

import com.hsf_project.dto.response.CinemaScheduleDTO;
import com.hsf_project.dto.response.CinemaScheduleResponse;
import com.hsf_project.entity.Cinema;
import com.hsf_project.entity.CinemaRoom;
import com.hsf_project.entity.City;
import com.hsf_project.service.impl.CinemaServiceImpl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


public interface CinemaRepository extends JpaRepository<Cinema,Integer> {


    @Query("""
                 SELECT new com.hsf_project.dto.response.CinemaScheduleDTO(
                      c.id,
                      c.address,
                      c.name,
                      cr.id,
                      cr.name,
                      cr.roomType,
                      st.id,
                      st.startTime,
                      st.endTime
                  )
                  FROM Cinema c
                  JOIN c.cinemaRooms cr
                  JOIN cr.showTimes st
                  WHERE c.city.id = :cityId
                    AND st.movie.id = :movieId
                    AND st.startTime >= :startDateTime
                    AND st.startTime < :nextDateTime
                    AND c.isDeleted = false
                    AND cr.isDeleted = false
                    AND st.isDeleted = false
            """)
    List<CinemaScheduleDTO> findCinemaSchedule(
            @Param("cityId") Integer cityId,
            @Param("movieId") Integer movieId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("nextDateTime") LocalDateTime nextDateTime
    );

}
