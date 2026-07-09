package com.hsf_project.controller;

import com.hsf_project.dto.MovieHomeDTO;
import com.hsf_project.dto.ReviewResponse;
import com.hsf_project.dto.request.CreateReviewRequest;
import com.hsf_project.entity.Genre;
import com.hsf_project.entity.Movie;
import com.hsf_project.entity.MovieStatus;
import com.hsf_project.exception.AppException;
import com.hsf_project.repository.movie.GenreRepository;
import com.hsf_project.service.MovieService;
import com.hsf_project.service.ReviewService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

@Controller
@RequestMapping({"/phim", "/movies"})
public class MovieController {

    @Autowired
    private MovieService movieService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private GenreRepository genreRepository;

    @GetMapping
    public String viewMovies(
            @RequestParam(defaultValue = "NOW_SHOWING") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "releaseDate") String sort,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) List<Integer> genreIds,
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

        boolean hasGenre = genreIds != null && !genreIds.isEmpty();
        boolean hasSearch = search != null && !search.trim().isEmpty();

        Page<MovieHomeDTO> moviePage;
        if (hasGenre && hasSearch) {
            moviePage = movieService.searchMoviesByGenre(genreIds, search.trim(), movieStatus, pageable);
        } else if (hasGenre) {
            moviePage = movieService.getMoviesByGenreAndStatus(genreIds, movieStatus, pageable);
        } else if (hasSearch) {
            moviePage = movieService.searchMovies(search.trim(), movieStatus, pageable);
        } else {
            moviePage = movieService.getMoviesByStatus(movieStatus, pageable);
        }

        List<Genre> allGenres = genreRepository.findByIsDeletedFalse();
        String genreIdsParam = hasGenre ? genreIds.stream().map(String::valueOf).collect(joining(",")) : null;

        model.addAttribute("moviePage", moviePage);
        model.addAttribute("currentStatus", movieStatus);
        model.addAttribute("search", search);
        model.addAttribute("sort", sort);
        model.addAttribute("direction", direction);
        model.addAttribute("selectedGenreIds", genreIds);
        model.addAttribute("genreIdsParam", genreIdsParam);
        model.addAttribute("allGenres", allGenres);
        model.addAttribute("activePage", "phim");

        return "movies";
    }

    @GetMapping("/{id}")
    public String viewMovieDetail(@PathVariable Integer id, Model model, HttpSession session) {
        Movie movie = movieService.getMovieById(id);

        String genresString = movie.getGenres().stream()
                .map(g -> g.getName())
                .collect(Collectors.joining(", "));

        List<ReviewResponse> reviews = reviewService.getReviewsByMovieId(id);

        model.addAttribute("movie", movie);
        model.addAttribute("genresString", genresString);
        model.addAttribute("reviews", reviews);
        model.addAttribute("activePage", "phim");

        com.hsf_project.entity.User currentUser = (com.hsf_project.entity.User) session.getAttribute("ttdn");
        if (currentUser != null) {
            boolean alreadyReviewed = reviewService.hasUserReviewed(id, currentUser.getId());
            model.addAttribute("alreadyReviewed", alreadyReviewed);
        }

        return "movie-detail";
    }

    @PostMapping("/{id}/review")
    public String submitReview(@PathVariable Integer id,
                               @Valid @ModelAttribute CreateReviewRequest request,
                               BindingResult bindingResult,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("reviewError", "Điểm đánh giá phải từ 1 đến 10");
            return "redirect:/phim/" + id;
        }

        com.hsf_project.entity.User currentUser = (com.hsf_project.entity.User) session.getAttribute("ttdn");
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("reviewError", "Vui lòng đăng nhập để đánh giá");
            return "redirect:/phim/" + id;
        }

        try {
            reviewService.createReview(id, currentUser.getId(), request.getRatingStar(), request.getComment());
            redirectAttributes.addFlashAttribute("reviewSuccess", "Đánh giá của bạn đã được gửi thành công!");
        } catch (AppException e) {
            redirectAttributes.addFlashAttribute("reviewError", e.getMessage());
        }

        return "redirect:/phim/" + id;
    }

    @GetMapping("/error")
    public String showMoviesPage(
            @RequestParam(value = "error", required = false) String error,
            Model model
    ) {
        if ("timeout".equals(error)) {
            // Đẩy một thông báo lỗi ra giao diện để báo cho khách biết lý do bị đá văng ra ngoài
            model.addAttribute("timeout", "Đã hết thời gian giữ ghế (15 phút). Vui lòng chọn lại suất chiếu!");
        }

        // Đoạn code lấy danh sách phim nowShowingMovies, comingSoonMovies... của em giữ nguyên

        return "home"; // Hoặc return "movies"; tùy theo file hiển thị danh sách phim của em
    }
}
