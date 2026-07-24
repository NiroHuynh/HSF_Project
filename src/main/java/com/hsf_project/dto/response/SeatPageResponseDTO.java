package com.hsf_project.dto.response;

import com.hsf_project.entity.ShowTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatPageResponseDTO {

    private ShowTime showtime;
    private List<SeatRowResponse> rows;
    private int totalSeats;
    private int availableSeats;
    private Map<String, BigDecimal> seatPriceMap;
}
