package com.hsf_project.service.impl;

import com.hsf_project.dto.response.SeatResponse;
import com.hsf_project.dto.response.SeatRowResponse;
import com.hsf_project.entity.Seat;
import com.hsf_project.repository.SeatRepository;
import com.hsf_project.repository.TicketRepository;
import com.hsf_project.service.SeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SeatServiceImpl implements SeatService {

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Override
    public List<SeatRowResponse> getSeatMap(Integer roomId,
                                       Long showtimeId) {


        List<Seat> seats =
                seatRepository.findSeatsByRoom(roomId);


        Map<String,List<SeatResponse>> grouped =
                seats.stream()
                        .collect(
                                Collectors.groupingBy(
                                        Seat::getRowLabel,
                                        LinkedHashMap::new,
                                        Collectors.mapping(
                                                seat -> new SeatResponse(
                                                        seat.getId(),
                                                        seat.getSeatCode(),
                                                        seat.getType(),
                                                        checkBooked(
                                                                seat.getId(),
                                                                showtimeId
                                                        )
                                                ),
                                                Collectors.toList()
                                        )
                                )
                        );


        return grouped.entrySet()
                .stream()
                .map(e ->
                        new SeatRowResponse(
                                e.getKey(),
                                e.getValue()
                        )
                )
                .toList();
    }

    boolean checkBooked(
            Long seatId,
            Long showtimeId
    ){
        return ticketRepository.existsBookedSeat(seatId,showtimeId);
    }

}
