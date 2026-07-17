package com.hsf_project.service;

import com.hsf_project.dto.MovieHomeDTO;
import com.hsf_project.entity.Movie;
import com.hsf_project.entity.enums.MovieStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MovieService {
    Movie getMovieById(Integer id);

    Page<MovieHomeDTO> getMoviesByStatus(MovieStatus status, Pageable pageable);

    Page<MovieHomeDTO> searchMovies(String keyword, MovieStatus status, Pageable pageable);

    Page<MovieHomeDTO> getMoviesByGenreAndStatus(List<Integer> genreIds, MovieStatus status, Pageable pageable);

    Page<MovieHomeDTO> searchMoviesByGenre(List<Integer> genreIds, String keyword, MovieStatus status, Pageable pageable);
}
