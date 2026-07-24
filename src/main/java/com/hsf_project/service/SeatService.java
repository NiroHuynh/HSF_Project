package com.hsf_project.service;

import com.hsf_project.dto.response.SeatPageResponseDTO;
import com.hsf_project.dto.response.SeatRowResponse;

import java.util.List;

public interface SeatService {
    List<SeatRowResponse> getSeatMap(Integer roomId, Long showtimeId);

    SeatPageResponseDTO getSeatPageData(Long showtimeId);

}
