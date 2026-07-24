package com.hsf_project.service.impl;

import com.hsf_project.dto.response.SeatPageResponseDTO;
import com.hsf_project.dto.response.SeatResponse;
import com.hsf_project.dto.response.SeatRowResponse;
import com.hsf_project.entity.Seat;
import com.hsf_project.entity.ShowTime;
import com.hsf_project.entity.TicketPrice;
import com.hsf_project.repository.SeatRepository;
import com.hsf_project.repository.ShowTimeRepository;
import com.hsf_project.repository.TicketRepository;
import com.hsf_project.service.SeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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

    @Autowired
    private ShowTimeRepository showTimeRepository;

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

    @Override
    public SeatPageResponseDTO getSeatPageData(Long showtimeId) {

        ShowTime showtime = showTimeRepository.findById(showtimeId)
                .orElseThrow(() -> new IllegalArgumentException("Khung giờ không tồn tại: " + showtimeId));

        List<SeatRowResponse> rows = getSeatMap(showtime.getRoom().getId(), showtimeId);

        int totalSeats = showtime.getRoom().getTotalSeats();
        int bookedCount = (int) rows.stream()
                .flatMap(r -> r.getSeats().stream())
                .filter(SeatResponse::isBooked)
                .count();

        int availableSeats = totalSeats - bookedCount;

        Map<String, BigDecimal> seatPriceMap = showtime.getRoom().getTicketPrices().stream()
                .collect(Collectors.toMap(TicketPrice::getSeatType, TicketPrice::getPrice));

        // Khởi tạo DTO bằng Lombok Builder (hoặc new SeatPageResponseDTO(...))
        return SeatPageResponseDTO.builder()
                .showtime(showtime)
                .rows(rows)
                .totalSeats(totalSeats)
                .availableSeats(availableSeats)
                .seatPriceMap(seatPriceMap)
                .build();
    }

    boolean checkBooked(
            Long seatId,
            Long showtimeId
    ){
        return ticketRepository.existsBookedSeat(seatId,showtimeId);
    }

}
