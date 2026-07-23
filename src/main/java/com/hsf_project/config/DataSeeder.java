package com.hsf_project.config;

import com.hsf_project.entity.Movie;
import com.hsf_project.repository.MovieRepository;
import com.hsf_project.service.MovieService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final MovieRepository movieRepository;
    private final MovieService movieService;

    @Override
    @Transactional
    public void run(String... args) {
        List<Movie> movies = movieRepository.findByIsDeletedFalseOrIsDeletedNull();
        boolean updated = false;

        for (Movie movie : movies) {
            if (movie.getEndDate() == null && movie.getReleaseDate() != null) {
                movie.setEndDate(movie.getReleaseDate().plusDays(30));
                updated = true;
            }
        }

        if (updated) {
            movieRepository.saveAll(movies);
            log.info("Đã cập nhật end_date (release + 30 ngày) cho {} phim",
                    movies.stream().filter(m -> m.getEndDate() != null).count());
        }

        movieService.autoUpdateMovieStatuses();
        log.info("Đã đồng bộ trạng thái phim theo ngày hiện tại");
    }
}