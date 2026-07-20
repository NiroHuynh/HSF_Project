package com.hsf_project.service.impl;

import com.hsf_project.dto.MovieHomeDTO;
import com.hsf_project.entity.Genre;
import com.hsf_project.entity.Movie;
import com.hsf_project.entity.enums.MovieStatus;
import com.hsf_project.entity.Promotion;
import com.hsf_project.repository.MovieRepository;
import com.hsf_project.repository.PromotionRepository;
import com.hsf_project.service.HomeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class HomeServiceImpl implements HomeService {

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private PromotionRepository promotionRepository;

    /**
     * Lấy danh sách phim theo trạng thái và gộp các thể loại thành chuỗi
     */
    @Override
    public List<MovieHomeDTO> getMoviesForHome(MovieStatus status) {

        List<MovieHomeDTO> resultList = new ArrayList<>();
        List<Movie> movieList = new ArrayList<>();

        if (status == MovieStatus.NOW_SHOWING) {

            movieList = movieRepository.findTopMoviesByRating(status.name());

        } else if (status == MovieStatus.COMING_SOON) {

            LocalDate today = LocalDate.now();
            movieList = movieRepository.findUpcomingMoviesByReleaseDate(status.name(), today);
        }

        for (Movie movie : movieList) {

            List<Genre> genreList = movie.getGenres();

            StringBuilder genresBuilder = new StringBuilder();

            for (int i = 0; i < genreList.size(); i++) {

                genresBuilder.append(genreList.get(i).getName());

                if (i < genreList.size() - 1) {
                    genresBuilder.append(", ");
                }
            }

            MovieHomeDTO dto = new MovieHomeDTO(movie, genresBuilder.toString());

            resultList.add(dto);
        }

        return resultList;
    }

    /**
     * Lấy danh sách các ưu đãi đang diễn ra
     */
    @Override
    public List<Promotion> getAvailablePromotions() {
        return promotionRepository.findActivePromotions(LocalDateTime.now());
    }
}