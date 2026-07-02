package com.hsf_project.service.impl;

import com.hsf_project.entity.Cinema;
import com.hsf_project.entity.CinemaRoom;
import com.hsf_project.repository.CinemaRepository;
import com.hsf_project.service.CinemaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class CinemaServiceImpl implements CinemaService {

    @Autowired
    private CinemaRepository cinemaRepository;

    @Override
    public List<Cinema> getCinemaByCityAndDate(Integer cityId) {
        return cinemaRepository.findCinemaByCity_Id(cityId);
    }

    @Override
    public List<Cinema> getCinemaWithRoomsByCityId(Integer cityId) {
        return cinemaRepository.findCinemaWithRoomsByCityId(cityId);
    }
}
