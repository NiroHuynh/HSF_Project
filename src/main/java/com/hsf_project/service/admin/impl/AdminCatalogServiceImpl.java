package com.hsf_project.service.admin.impl;

import com.hsf_project.entity.Genre;
import com.hsf_project.entity.Movie;
import com.hsf_project.entity.Promotion;
import com.hsf_project.repository.GenreRepository;
import com.hsf_project.repository.admin.AdminMovieRepository;
import com.hsf_project.repository.admin.AdminPromotionRepository;
import com.hsf_project.service.admin.AdminCatalogService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class AdminCatalogServiceImpl implements AdminCatalogService {
    private final AdminMovieRepository movies;
    private final AdminPromotionRepository promotions;
    private final GenreRepository genres;

    public AdminCatalogServiceImpl(AdminMovieRepository movies, AdminPromotionRepository promotions,
                                   GenreRepository genres) {
        this.movies = movies;
        this.promotions = promotions;
        this.genres = genres;
    }

    public List<Movie> getMovies() { return movies.findAllVisible(); }
    public Movie getMovie(Integer id) {
        return movies.findById(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phim."));
    }
    public Movie saveMovie(Movie form, List<Integer> genreIds) {
        Movie movie = form.getId() == null ? new Movie() : getMovie(form.getId());
        movie.setTitle(form.getTitle()); movie.setDescription(form.getDescription());
        movie.setDurationMinutes(form.getDurationMinutes()); movie.setDirector(form.getDirector());
        movie.setCast(form.getCast()); movie.setReleaseDate(form.getReleaseDate());
        movie.setPosterUrl(form.getPosterUrl()); movie.setAgeRating(form.getAgeRating());
        movie.setStatus(form.getStatus());
        if (movie.getId() == null && movie.getAverageRating() == null) {
            movie.setAverageRating(BigDecimal.ZERO);
        }
        movie.setDeleted(false);
        if (movie.getCreatedAt() == null) movie.setCreatedAt(LocalDateTime.now());
        List<Genre> selected = genreIds == null ? List.of() : genres.findAllById(genreIds);
        movie.setGenres(selected);
        return movies.save(movie);
    }
    public void deleteMovie(Integer id) {
        Movie movie = getMovie(id); movie.setDeleted(true); movies.save(movie);
    }
    public List<Promotion> getPromotions() { return promotions.findAllVisible(); }
    public Promotion getPromotion(Long id) {
        return promotions.findById(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy voucher."));
    }
    public Promotion savePromotion(Promotion form) {
        Promotion promotion = form.getId() == null ? new Promotion() : getPromotion(form.getId());
        promotions.findByCodeIgnoreCase(form.getCode())
                .filter(existing -> !existing.getId().equals(form.getId()))
                .ifPresent(existing -> { throw new IllegalArgumentException("Mã voucher đã tồn tại."); });
        promotion.setCode(form.getCode().trim().toUpperCase()); promotion.setName(form.getName());
        promotion.setDescription(form.getDescription()); promotion.setDiscountType(form.getDiscountType());
        promotion.setDiscountValue(form.getDiscountValue()); promotion.setStartDate(form.getStartDate());
        promotion.setEndDate(form.getEndDate()); promotion.setUsageLimit(form.getUsageLimit());
        promotion.setUsedCount(form.getUsedCount() == null ? 0 : form.getUsedCount());
        promotion.setStatus(form.getStatus()); promotion.setIsDeleted(false);
        return promotions.save(promotion);
    }
    public void deletePromotion(Long id) {
        Promotion promotion = getPromotion(id); promotion.setIsDeleted(true); promotions.save(promotion);
    }
}
