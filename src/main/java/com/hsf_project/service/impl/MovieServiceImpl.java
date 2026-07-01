package com.hsf_project.service.impl;

import com.hsf_project.dto.MovieHomeDTO;
import com.hsf_project.entity.Movie;
import com.hsf_project.entity.MovieStatus;
import com.hsf_project.exception.AppException;
import com.hsf_project.exception.ErrorCode;
import com.hsf_project.mapper.MovieMapper;
import com.hsf_project.repository.movie.MovieRepository;
import com.hsf_project.service.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MovieServiceImpl implements MovieService {

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private MovieMapper movieMapper;

    @Override
    public Movie getMovieById(Integer id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.MOVIE_NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MovieHomeDTO> getMoviesByStatus(MovieStatus status, Pageable pageable) {
        Page<Movie> moviePage = movieRepository.findByStatusAndIsDeletedFalse(status, pageable);
        return moviePage.map(movieMapper::toMovieHomeDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MovieHomeDTO> searchMovies(String keyword, MovieStatus status, Pageable pageable) {
        Page<Movie> moviePage = movieRepository
                .findByTitleContainingIgnoreCaseAndStatusAndIsDeletedFalse(keyword, status, pageable);
        return moviePage.map(movieMapper::toMovieHomeDTO);
    }
}
