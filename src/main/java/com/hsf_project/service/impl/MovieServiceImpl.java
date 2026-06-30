package com.hsf_project.service.impl;

import com.hsf_project.entity.Movie;
import com.hsf_project.repository.MovieRepository;
import com.hsf_project.service.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MovieServiceImpl implements MovieService {

    @Autowired
    private MovieRepository movieRepository;

    @Override
    public Movie getMovieById(Integer id) {
        return movieRepository.findById(id).get();
    }
}
