package com.hsf_project.controller;

import com.hsf_project.dto.MovieHomeDTO;
import com.hsf_project.entity.Movie;
import com.hsf_project.entity.MovieReview;
import com.hsf_project.entity.MovieStatus;
import com.hsf_project.exception.AppException;
import com.hsf_project.exception.ErrorCode;
import com.hsf_project.repository.MovieReviewRepository;
import com.hsf_project.service.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping({"/phim", "/movies"})
public class MovieController {

    @Autowired
    private MovieService movieService;

    @Autowired
    private MovieReviewRepository movieReviewRepository;

    @GetMapping
    public String viewMovies(
            @RequestParam(defaultValue = "NOW_SHOWING") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "releaseDate") String sort,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String search,
            Model model) {

        MovieStatus movieStatus;
        try {
            movieStatus = MovieStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            movieStatus = MovieStatus.NOW_SHOWING;
        }

        Sort sortObj = direction.equalsIgnoreCase("asc")
                ? Sort.by(sort).ascending()
                : Sort.by(sort).descending();
        PageRequest pageable = PageRequest.of(page, size, sortObj);

        Page<MovieHomeDTO> moviePage;
        if (search != null && !search.trim().isEmpty()) {
            moviePage = movieService.searchMovies(search.trim(), movieStatus, pageable);
        } else {
            moviePage = movieService.getMoviesByStatus(movieStatus, pageable);
        }

        model.addAttribute("moviePage", moviePage);
        model.addAttribute("currentStatus", movieStatus);
        model.addAttribute("search", search);
        model.addAttribute("sort", sort);
        model.addAttribute("direction", direction);
        model.addAttribute("activePage", "phim");

        return "movies";
    }

    @GetMapping("/{id}")
    public String viewMovieDetail(@PathVariable Integer id, Model model) {
        Movie movie = movieService.getMovieById(id);

        String genresString = movie.getGenres().stream()
                .map(g -> g.getName())
                .collect(Collectors.joining(", "));

        List<MovieReview> reviews = movieReviewRepository
                .findByMovieIdAndIsDeletedFalseOrderByCreatedAtDesc(id);

        model.addAttribute("movie", movie);
        model.addAttribute("genresString", genresString);
        model.addAttribute("reviews", reviews);
        model.addAttribute("activePage", "phim");

        return "movie-detail";
    }
}
