package com.hsf_project.controller.admin;

import com.hsf_project.dto.common.ApiResponse;
import com.hsf_project.dto.movie.request.ChangeStatusRequest;
import com.hsf_project.dto.movie.request.CreateMovieRequest;
import com.hsf_project.dto.movie.request.UpdateMovieRequest;
import com.hsf_project.dto.movie.response.MovieResponse;
import com.hsf_project.entity.Movie;
import com.hsf_project.entity.MovieStatus;
import com.hsf_project.entity.User;
import com.hsf_project.mapper.MovieAdminMapper;
import com.hsf_project.service.MovieService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/admin/movies")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class AdminMovieController {

    MovieService movieService;
    MovieAdminMapper movieAdminMapper;

    @GetMapping
    public ApiResponse<Page<MovieResponse>> getMovies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "releaseDate") String sort,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) List<Integer> genreIds) {

        if (size > 100) size = 100;

        MovieStatus movieStatus = null;
        if (status != null) {
            try {
                movieStatus = MovieStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ApiResponse.error(1002, "Trạng thái phim không hợp lệ");
            }
        }

        Sort sortObj = direction.equalsIgnoreCase("asc")
                ? Sort.by(sort).ascending()
                : Sort.by(sort).descending();
        PageRequest pageable = PageRequest.of(page, size, sortObj);

        Page<MovieResponse> result = movieService.searchMoviesAdmin(search, movieStatus, genreIds, pageable)
                .map(movieAdminMapper::toMovieResponse);

        return ApiResponse.success(result);
    }

    @GetMapping("/{id}")
    public ApiResponse<MovieResponse> getMovie(@PathVariable Integer id) {
        return ApiResponse.success(movieAdminMapper.toMovieResponse(movieService.getMovieByIdAdmin(id)));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<MovieResponse> createMovie(@Valid @RequestBody CreateMovieRequest request,
                                                   HttpServletRequest httpRequest) {
        String updatedBy = getCurrentAdminName(httpRequest);
        return ApiResponse.success(movieService.createMovie(request, updatedBy));
    }

    @PutMapping("/{id}")
    public ApiResponse<MovieResponse> updateMovie(@PathVariable Integer id,
                                                    @Valid @RequestBody UpdateMovieRequest request,
                                                    HttpServletRequest httpRequest) {
        String updatedBy = getCurrentAdminName(httpRequest);
        return ApiResponse.success(movieService.updateMovie(id, request, updatedBy));
    }

    private String getCurrentAdminName(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            User currentUser = (User) session.getAttribute("ttdn");
            if (currentUser != null) {
                return currentUser.getFullName();
            }
        }
        return "Hệ thống";
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteMovie(@PathVariable Integer id) {
        movieService.deleteMovie(id);
        return ApiResponse.success(null);
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<MovieResponse> changeStatus(@PathVariable Integer id,
                                                    @Valid @RequestBody ChangeStatusRequest request) {
        return ApiResponse.success(movieService.changeMovieStatus(id, request.getStatus()));
    }

    @PostMapping("/{id}/poster")
    public ApiResponse<String> uploadPoster(@PathVariable Integer id,
                                             @RequestParam("file") MultipartFile file) {
        return ApiResponse.success(movieService.uploadPoster(id, file));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<MovieResponse> cancelMovie(@PathVariable Integer id) {
        return ApiResponse.success(movieService.cancelMovie(id));
    }

    @PostMapping("/seed-posters")
    public ApiResponse<Integer> uploadSeedPosters() {
        int count = movieService.uploadSeedPosters();
        return ApiResponse.success(count);
    }
}
