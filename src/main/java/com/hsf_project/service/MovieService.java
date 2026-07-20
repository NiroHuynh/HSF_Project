package com.hsf_project.service;

import com.hsf_project.dto.MovieHomeDTO;
import com.hsf_project.dto.movie.request.CreateMovieRequest;
import com.hsf_project.dto.movie.request.UpdateMovieRequest;
import com.hsf_project.dto.movie.response.MovieResponse;
import com.hsf_project.entity.Movie;
import com.hsf_project.entity.enums.MovieStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MovieService {
    Movie getMovieById(Integer id);

    Movie getMovieByIdAdmin(Integer id);

    Page<Movie> searchMoviesAdmin(String search, MovieStatus status, List<Integer> genreIds, Pageable pageable);

    Page<MovieHomeDTO> getMoviesByStatus(MovieStatus status, Pageable pageable);

    Page<MovieHomeDTO> searchMovies(String keyword, MovieStatus status, Pageable pageable);

    Page<MovieHomeDTO> getMoviesByGenreAndStatus(List<Integer> genreIds, MovieStatus status, Pageable pageable);

    Page<MovieHomeDTO> searchMoviesByGenre(List<Integer> genreIds, String keyword, MovieStatus status, Pageable pageable);

    MovieResponse createMovie(CreateMovieRequest request, String updatedBy);

    MovieResponse updateMovie(Integer id, UpdateMovieRequest request, String updatedBy);

    void deleteMovie(Integer id);

    MovieResponse changeMovieStatus(Integer id, String newStatus);

    MovieResponse cancelMovie(Integer id);

    void autoUpdateMovieStatuses();

    String uploadPoster(Integer id, MultipartFile file);

    int uploadSeedPosters();
}
