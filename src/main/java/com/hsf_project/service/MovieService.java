package com.hsf_project.service;

import com.hsf_project.dto.MovieHomeDTO;
import com.hsf_project.entity.Movie;
import com.hsf_project.entity.MovieStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MovieService {
    Movie getMovieById(Integer id);

    Page<MovieHomeDTO> getMoviesByStatus(MovieStatus status, Pageable pageable);

    Page<MovieHomeDTO> searchMovies(String keyword, MovieStatus status, Pageable pageable);
}
