package com.hsf_project.service.impl;

import com.hsf_project.dto.response.CinemaScheduleDTO;
import com.hsf_project.dto.response.CinemaScheduleResponse;
import com.hsf_project.dto.response.RoomScheduleResponse;
import com.hsf_project.dto.response.ShowTimeScheduleResponse;
import com.hsf_project.entity.Cinema;
import com.hsf_project.entity.CinemaRoom;
import com.hsf_project.repository.CinemaRepository;
import com.hsf_project.service.CinemaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CinemaServiceImpl implements CinemaService {

    @Autowired
    private CinemaRepository cinemaRepository;

    @Override
    public List<CinemaScheduleResponse> getCinemaByCityAndDateAndMovie(Integer cityId, LocalDate selectDate, Integer movieId) {
        LocalDate date = selectDate;

        LocalDateTime start = date != null ? date.atStartOfDay() : null;
        LocalDateTime end = date != null ? date.plusDays(1).atStartOfDay() : null;

         List<CinemaScheduleDTO> cinema = cinemaRepository.findCinemaSchedule(
                cityId,
                movieId,
                start,
                end
        );

        List<CinemaScheduleResponse> cinemaSchedule = convert(cinema);

        return cinemaSchedule;

    }

    public List<CinemaScheduleResponse> convert(
            List<CinemaScheduleDTO> flatList
    ) {

        Map<Integer, CinemaScheduleResponse> cinemaMap =
                new LinkedHashMap<>();

        for (CinemaScheduleDTO item : flatList) {

            CinemaScheduleResponse cinema =
                    cinemaMap.computeIfAbsent(
                            item.getCinemaId(),
                            id -> new CinemaScheduleResponse(
                                    id,
                                    item.getCinemaName(),
                                    item.getAddress(),
                                    new ArrayList<>()
                            )
                    );


            RoomScheduleResponse room =
                    cinema.getRooms()
                            .stream()
                            .filter(r ->
                                    r.getRoomId()
                                            .equals(item.getRoomId())
                            )
                            .findFirst()
                            .orElseGet(() -> {

                                RoomScheduleResponse newRoom =
                                        new RoomScheduleResponse(
                                                item.getRoomId(),
                                                item.getRoomName(),
                                                item.getRoomType(),
                                                new ArrayList<>()
                                        );

                                cinema.getRooms()
                                        .add(newRoom);

                                return newRoom;
                            });


            room.getShowTimes()
                    .add(
                            new ShowTimeScheduleResponse(
                                    item.getShowTimeId(),
                                    item.getStartTime(),
                                    item.getEndTime()
                            )
                    );
        }

        return new ArrayList<>(cinemaMap.values());
    }

}



