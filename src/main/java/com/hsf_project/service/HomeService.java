package com.hsf_project.service;

import com.hsf_project.dto.MovieHomeDTO;
import com.hsf_project.entity.enums.MovieStatus;
import com.hsf_project.entity.Promotion;

import java.util.List;

public interface HomeService {

    List<MovieHomeDTO> getMoviesForHome(MovieStatus status);

    List<Promotion> getAvailablePromotions();
}