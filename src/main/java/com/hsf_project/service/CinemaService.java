package com.hsf_project.service;

import com.hsf_project.dto.response.CinemaScheduleResponse;
import com.hsf_project.entity.Cinema;
import com.hsf_project.entity.CinemaRoom;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface CinemaService {
    List<Cinema> getCinemaByCityAndDate(Integer cityId);
    List<Cinema> getCinemaWithRoomsByCityId(Integer cityId);
    List<CinemaScheduleResponse> getCinemaByCityAndDateAndMovie(Integer cityId, LocalDate selectDate, Integer id);
}
