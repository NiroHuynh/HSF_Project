package com.hsf_project.service.admin;

import com.hsf_project.entity.Movie;
import com.hsf_project.entity.Promotion;
import java.util.List;

public interface AdminCatalogService {
    List<Movie> getMovies();
    Movie getMovie(Integer id);
    Movie saveMovie(Movie movie, List<Integer> genreIds);
    void deleteMovie(Integer id);
    List<Promotion> getPromotions();
    Promotion getPromotion(Long id);
    Promotion savePromotion(Promotion promotion);
    void deletePromotion(Long id);
}
