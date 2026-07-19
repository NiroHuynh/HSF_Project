package com.hsf_project.controller.admin;

import com.hsf_project.entity.Movie;
import com.hsf_project.entity.enums.AgeRating;
import com.hsf_project.entity.enums.MovieStatus;
import com.hsf_project.repository.GenreRepository;
import com.hsf_project.service.admin.AdminCatalogService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
@RequestMapping("/admin/movies")
public class AdminMovieController {
    private final AdminCatalogService catalog;
    private final GenreRepository genres;

    public AdminMovieController(AdminCatalogService catalog, GenreRepository genres) {
        this.catalog = catalog;
        this.genres = genres;
    }

    @GetMapping
    public String list(@RequestParam(required = false) Integer edit, Model model) {
        model.addAttribute("movies", catalog.getMovies());
        model.addAttribute("movie", edit == null ? new Movie() : catalog.getMovie(edit));
        model.addAttribute("genres", genres.findAll());
        model.addAttribute("ageRatings", AgeRating.values());
        model.addAttribute("movieStatuses", MovieStatus.values());
        model.addAttribute("activePage", "movies");
        return "admin/movies";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Movie movie,
                       @RequestParam(required = false) List<Integer> genreIds,
                       RedirectAttributes flash) {
        try {
            catalog.saveMovie(movie, genreIds);
            flash.addFlashAttribute("success", movie.getId() == null ? "Đã thêm phim." : "Đã cập nhật phim.");
        } catch (RuntimeException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/movies";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Integer id, RedirectAttributes flash) {
        catalog.deleteMovie(id);
        flash.addFlashAttribute("success", "Đã ẩn phim khỏi hệ thống.");
        return "redirect:/admin/movies";
    }
}
