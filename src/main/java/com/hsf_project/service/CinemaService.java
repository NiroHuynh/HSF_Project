package com.hsf_project.service;

import com.hsf_project.entity.Cinema;
import com.hsf_project.entity.CinemaRoom;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface CinemaService {
    List<Cinema> getCinemaByCityAndDate(Integer cityId);
    List<Cinema> getCinemaWithRoomsByCityId(Integer cityId);
}
