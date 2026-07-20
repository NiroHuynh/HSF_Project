package com.hsf_project.service.impl;

import com.hsf_project.dto.MovieHomeDTO;
import com.hsf_project.dto.movie.request.CreateMovieRequest;
import com.hsf_project.dto.movie.request.UpdateMovieRequest;
import com.hsf_project.dto.movie.response.MovieResponse;
import com.hsf_project.entity.*;
import com.hsf_project.exception.AppException;
import com.hsf_project.exception.ErrorCode;
import com.hsf_project.mapper.MovieAdminMapper;
import com.hsf_project.mapper.MovieMapper;
import com.hsf_project.repository.*;
import com.hsf_project.repository.movie.GenreRepository;
import com.hsf_project.repository.movie.MovieRepository;
import com.hsf_project.service.CloudinaryService;
import com.hsf_project.service.MovieService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class MovieServiceImpl implements MovieService {

    MovieRepository movieRepository;
    GenreRepository genreRepository;
    ShowTimeRepository showTimeRepository;
    BookingRepository bookingRepository;
    NotificationRepository notificationRepository;
    UserNotificationRepository userNotificationRepository;
    MovieAdminMapper movieAdminMapper;
    MovieMapper movieMapper;
    CloudinaryService cloudinaryService;

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of("image/jpeg", "image/jpg", "image/png", "image/webp");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    @Override
    public Movie getMovieById(Integer id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.MOVIE_NOT_FOUND));
    }

    @Override
    public Movie getMovieByIdAdmin(Integer id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.MOVIE_NOT_FOUND));
        if (Boolean.TRUE.equals(movie.getIsDeleted())) {
            throw new AppException(ErrorCode.MOVIE_NOT_FOUND);
        }
        return movie;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Movie> searchMoviesAdmin(String search, MovieStatus status, List<Integer> genreIds, Pageable pageable) {
        String keyword = (search != null && !search.trim().isEmpty()) ? search.trim() : null;

        List<Movie> movies = status != null
                ? movieRepository.findByStatusAndIsDeletedFalse(status)
                : movieRepository.findByIsDeletedFalseOrIsDeletedNull();

        List<Movie> filtered = movies.stream()
                .filter(m -> matchesKeyword(m, keyword))
                .filter(m -> matchesGenreIds(m, genreIds))
                .toList();

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filtered.size());
        List<Movie> pageContent = start < filtered.size()
                ? filtered.subList(start, end)
                : List.of();
        return new PageImpl<>(pageContent, pageable, filtered.size());
    }

    private boolean matchesKeyword(Movie movie, String keyword) {
        if (keyword == null || keyword.isBlank()) return true;
        String kw = keyword.toLowerCase();
        return (movie.getTitle() != null && movie.getTitle().toLowerCase().contains(kw))
                || (movie.getDirector() != null && movie.getDirector().toLowerCase().contains(kw))
                || (movie.getCast() != null && movie.getCast().toLowerCase().contains(kw));
    }

    private boolean matchesGenreIds(Movie movie, List<Integer> genreIds) {
        if (genreIds == null || genreIds.isEmpty()) return true;
        return movie.getGenres().stream().anyMatch(g -> genreIds.contains(g.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MovieHomeDTO> getMoviesByStatus(MovieStatus status, Pageable pageable) {
        return movieRepository.findByStatusAndIsDeletedFalse(status, pageable)
                .map(movieMapper::toMovieHomeDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MovieHomeDTO> searchMovies(String keyword, MovieStatus status, Pageable pageable) {
        return movieRepository.findByTitleContainingIgnoreCaseAndStatusAndIsDeletedFalse(keyword, status, pageable)
                .map(movieMapper::toMovieHomeDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MovieHomeDTO> getMoviesByGenreAndStatus(List<Integer> genreIds, MovieStatus status, Pageable pageable) {
        return movieRepository.findDistinctByGenres_IdInAndStatusAndIsDeletedFalse(genreIds, status, pageable)
                .map(movieMapper::toMovieHomeDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MovieHomeDTO> searchMoviesByGenre(List<Integer> genreIds, String keyword, MovieStatus status, Pageable pageable) {
        return movieRepository.findDistinctByGenres_IdInAndStatusAndIsDeletedFalseAndTitleContainingIgnoreCase(genreIds, status, keyword, pageable)
                .map(movieMapper::toMovieHomeDTO);
    }

    @Override
    @Transactional
    public MovieResponse createMovie(CreateMovieRequest request, String updatedBy) {
        if (movieRepository.existsByTitleIgnoreCaseAndIsDeletedFalse(request.getTitle().trim()))
            throw new AppException(ErrorCode.MOVIE_TITLE_EXISTS);

        if (request.getReleaseDate().isBefore(LocalDate.now()))
            throw new AppException(ErrorCode.INVALID_RELEASE_DATE);

        if (request.getEndDate() != null && !request.getEndDate().isAfter(request.getReleaseDate()))
            throw new AppException(ErrorCode.INVALID_DATE_RANGE, "Ngày kết thúc phải sau ngày khởi chiếu");
        if (request.getEndDate() != null && request.getEndDate().isBefore(LocalDate.now()))
            throw new AppException(ErrorCode.INVALID_DATE_RANGE, "Ngày kết thúc không được trong quá khứ");

        List<Genre> genres = validateGenres(request.getGenreIds());

        Movie movie = movieAdminMapper.toMovie(request);
        if (request.getStatus() != null) {
            movie.setStatus(request.getStatus());
        } else if (!request.getReleaseDate().isAfter(LocalDate.now())) {
            movie.setStatus(MovieStatus.NOW_SHOWING);
        } else {
            movie.setStatus(MovieStatus.COMING_SOON);
        }
        movie.setAgeRating(request.getAgeRating() != null ? request.getAgeRating() : AgeRating.P);
        movie.setIsDeleted(false);
        movie.setCreatedAt(LocalDateTime.now());
        movie.setUpdatedBy(updatedBy);
        movie.setUpdatedAt(LocalDateTime.now());
        movie.setGenres(genres);
        movie.setLanguage(request.getLanguage());
        movie.setEndDate(request.getEndDate());
        movie.setIsFeatured(request.getIsFeatured() != null && request.getIsFeatured());

        return movieAdminMapper.toMovieResponse(movieRepository.save(movie));
    }

    @Override
    @Transactional
    public MovieResponse updateMovie(Integer id, UpdateMovieRequest request, String updatedBy) {
        Movie movie = getMovieByIdAdmin(id);

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            movieRepository.findByTitleIgnoreCaseAndIsDeletedFalse(request.getTitle().trim())
                    .ifPresent(existing -> {
                        if (!existing.getId().equals(id))
                            throw new AppException(ErrorCode.MOVIE_TITLE_EXISTS);
                    });
        }

        if (movie.getStatus() == MovieStatus.ENDED || movie.getStatus() == MovieStatus.CANCELLED)
            throw new AppException(ErrorCode.INVALID_STATUS_TRANSITION,
                    "Không thể cập nhật phim đã " +
                    (movie.getStatus() == MovieStatus.ENDED ? "kết thúc" : "hủy"));

        if (request.getDurationMinutes() != null
                && !showTimeRepository.findByMovieIdAndIsDeletedFalse(id).isEmpty())
            throw new AppException(ErrorCode.MOVIE_NOW_SHOWING, "Không thể thay đổi thời lượng khi phim đã có suất chiếu");

        if (request.getReleaseDate() != null) {
            if (movie.getStatus() == MovieStatus.NOW_SHOWING)
                throw new AppException(ErrorCode.MOVIE_NOW_SHOWING, "Không thể thay đổi ngày khởi chiếu khi phim đang chiếu");
            if (request.getReleaseDate().isBefore(LocalDate.now()))
                throw new AppException(ErrorCode.INVALID_RELEASE_DATE);
            if (movie.getStatus() == MovieStatus.COMING_SOON && !request.getReleaseDate().isAfter(LocalDate.now())) {
                movie.setStatus(MovieStatus.NOW_SHOWING);
            }
        }

        if (request.getEndDate() != null) {
            LocalDate refRelease = request.getReleaseDate() != null ? request.getReleaseDate() : movie.getReleaseDate();
            if (!request.getEndDate().isAfter(refRelease))
                throw new AppException(ErrorCode.INVALID_DATE_RANGE, "Ngày kết thúc phải sau ngày khởi chiếu");
        }

        if (request.getGenreIds() != null && !request.getGenreIds().isEmpty()) {
            movie.setGenres(validateGenres(request.getGenreIds()));
        }

        movieAdminMapper.updateMovie(request, movie);

        if (request.getLanguage() != null) movie.setLanguage(request.getLanguage());
        if (request.getEndDate() != null) movie.setEndDate(request.getEndDate());
        if (request.getIsFeatured() != null) movie.setIsFeatured(request.getIsFeatured());

        if (request.getStatus() != null) {
            MovieStatus target = request.getStatus();
            MovieStatus current = movie.getStatus();
            if (current != target) {
                if (current == MovieStatus.ENDED || current == MovieStatus.CANCELLED)
                    throw new AppException(ErrorCode.INVALID_STATUS_TRANSITION,
                            "Không thể thay đổi trạng thái phim đã " +
                            (current == MovieStatus.ENDED ? "kết thúc" : "hủy"));
                boolean valid = (current == MovieStatus.COMING_SOON && (target == MovieStatus.NOW_SHOWING || target == MovieStatus.ENDED))
                        || (current == MovieStatus.NOW_SHOWING && target == MovieStatus.ENDED);
                if (!valid)
                    throw new AppException(ErrorCode.INVALID_STATUS_TRANSITION,
                            "Không thể chuyển từ " + current + " sang " + target);
                if (target == MovieStatus.ENDED
                        && showTimeRepository.countUpcomingByMovie(id, LocalDateTime.now()) > 0)
                    throw new AppException(ErrorCode.INVALID_STATUS_TRANSITION,
                            "Không thể kết thúc phim khi còn suất chiếu trong tương lai");
                movie.setStatus(target);
            }
        }

        movie.setUpdatedBy(updatedBy);
        movie.setUpdatedAt(LocalDateTime.now());

        return movieAdminMapper.toMovieResponse(movieRepository.save(movie));
    }

    @Override
    @Transactional
    public void deleteMovie(Integer id) {
        Movie movie = getMovieByIdAdmin(id);

        long upcomingShowtimes = showTimeRepository.countUpcomingByMovie(id, LocalDateTime.now());
        if (upcomingShowtimes > 0)
            throw new AppException(ErrorCode.MOVIE_HAS_SHOWTIMES);
        if (bookingRepository.hasConfirmedBookingsByMovie(id))
            throw new AppException(ErrorCode.MOVIE_HAS_BOOKINGS);

        movie.setIsDeleted(true);
        movieRepository.save(movie);
    }

    @Override
    @Transactional
    public MovieResponse changeMovieStatus(Integer id, String newStatus) {
        Movie movie = getMovieByIdAdmin(id);
        MovieStatus current = movie.getStatus();
        MovieStatus target;

        try {
            target = MovieStatus.valueOf(newStatus.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new AppException(ErrorCode.INVALID_MOVIE_STATUS);
        }

        if (current == target)
            throw new AppException(ErrorCode.INVALID_STATUS_TRANSITION, "Phim đã ở trạng thái " + current);
        if (current == MovieStatus.ENDED || current == MovieStatus.CANCELLED)
            throw new AppException(ErrorCode.INVALID_STATUS_TRANSITION, "Không thể thay đổi trạng thái phim đã " + (current == MovieStatus.ENDED ? "kết thúc" : "hủy"));
        if (target == MovieStatus.CANCELLED)
            throw new AppException(ErrorCode.INVALID_STATUS_TRANSITION, "Vui lòng sử dụng API hủy phim");

        boolean valid = (current == MovieStatus.COMING_SOON && (target == MovieStatus.NOW_SHOWING || target == MovieStatus.ENDED))
                || (current == MovieStatus.NOW_SHOWING && target == MovieStatus.ENDED);
        if (!valid)
            throw new AppException(ErrorCode.INVALID_STATUS_TRANSITION, "Không thể chuyển từ " + current + " sang " + target);
        if (target == MovieStatus.ENDED
                && showTimeRepository.countUpcomingByMovie(id, LocalDateTime.now()) > 0)
            throw new AppException(ErrorCode.NO_FUTURE_SHOWTIMES, "Không thể kết thúc phim khi còn suất chiếu trong tương lai");

        movie.setStatus(target);
        return movieAdminMapper.toMovieResponse(movieRepository.save(movie));
    }

    @Override
    @Transactional
    public MovieResponse cancelMovie(Integer id) {
        Movie movie = getMovieByIdAdmin(id);

        if (movie.getStatus() != MovieStatus.NOW_SHOWING && movie.getStatus() != MovieStatus.COMING_SOON)
            throw new AppException(ErrorCode.MOVIE_CANNOT_CANCEL);

        if (bookingRepository.hasConfirmedBookingsByMovie(id))
            throw new AppException(ErrorCode.MOVIE_HAS_BOOKINGS, "Không thể hủy phim đã có vé đặt");

        movie.setStatus(MovieStatus.CANCELLED);
        movieRepository.save(movie);

        createCancellationNotifications(movie);
        return movieAdminMapper.toMovieResponse(movie);
    }

    @Override
    @Scheduled(fixedRate = 300000)
    @Transactional
    public void autoUpdateMovieStatuses() {
        LocalDate today = LocalDate.now();

        List<Movie> comingSoon = movieRepository.findByStatusAndReleaseDateLessThanEqualAndIsDeletedFalse(MovieStatus.COMING_SOON, today);
        for (Movie m : comingSoon) {
            m.setStatus(MovieStatus.NOW_SHOWING);
        }
        movieRepository.saveAll(comingSoon);

        List<Movie> nowShowing = movieRepository.findByStatusAndEndDateBeforeAndIsDeletedFalse(MovieStatus.NOW_SHOWING, today);
        for (Movie m : nowShowing) {
            m.setStatus(MovieStatus.ENDED);
        }
        movieRepository.saveAll(nowShowing);
    }

    @Override
    @Transactional
    public String uploadPoster(Integer id, MultipartFile file) {
        Movie movie = getMovieByIdAdmin(id);

        if (file.isEmpty())
            throw new AppException(ErrorCode.INVALID_FILE_FORMAT, "File không được để trống");

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase()))
            throw new AppException(ErrorCode.INVALID_FILE_FORMAT);

        if (file.getSize() > MAX_FILE_SIZE)
            throw new AppException(ErrorCode.FILE_TOO_LARGE);

        try {
            String url = cloudinaryService.uploadImage(file, "movie_posters");
            movie.setPosterUrl(url);
            movieRepository.save(movie);
            return url;
        } catch (IOException e) {
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Không thể upload ảnh: " + e.getMessage());
        }
    }

    private void createCancellationNotifications(Movie movie) {
        Notification notification = new Notification();
        notification.setTitle("Phim đã bị hủy");
        notification.setContent("Bộ phim \"" + movie.getTitle() + "\" đã bị hủy.");
        notification.setType("MOVIE_CANCEL");
        notification.setIsDeleted(false);
        notification.setCreatedAt(LocalDateTime.now());
        notification = notificationRepository.save(notification);

        List<Long> userIds = showTimeRepository.findDistinctUserIdsByMovieId(movie.getId());
        for (Long userId : userIds) {
            UserNotification un = new UserNotification();
            un.setId(new UserNotificationId());
            un.getId().setUserId(userId);
            un.getId().setNotificationId(notification.getId());

            User user = new User();
            user.setId(userId);
            un.setUser(user);
            un.setNotification(notification);
            un.setReceiveAt(LocalDateTime.now());
            userNotificationRepository.save(un);
        }
    }

    private List<Genre> validateGenres(List<Integer> genreIds) {
        List<Genre> genres = genreRepository.findAllById(genreIds);
        if (genres.size() != genreIds.size())
            throw new AppException(ErrorCode.INVALID_GENRE);
        for (Genre g : genres) {
            if (Boolean.TRUE.equals(g.getIsDeleted()))
                throw new AppException(ErrorCode.INVALID_GENRE, "Thể loại \"" + g.getName() + "\" đã bị xóa");
        }
        return genres;
    }

    @Override
    @Transactional
    public int uploadSeedPosters() {
        List<Movie> movies = movieRepository.findByIsDeletedFalseOrIsDeletedNull();
        int count = 0;
        for (Movie movie : movies) {
            String posterUrl = movie.getPosterUrl();
            if (posterUrl != null && posterUrl.startsWith("/images/")) {
                try {
                    String filename = posterUrl.substring("/images/".length());
                    ClassPathResource resource = new ClassPathResource("static/images/" + filename);
                    if (!resource.exists()) continue;

                    byte[] fileBytes;
                    try (InputStream is = resource.getInputStream()) {
                        fileBytes = is.readAllBytes();
                    }

                    String publicId = "movie_" + movie.getId() + "_" + filename.replace('.', '_');
                    String url = cloudinaryService.uploadImageBytes(fileBytes, "movie_posters", publicId);

                    movie.setPosterUrl(url);
                    movieRepository.save(movie);
                    count++;
                } catch (Exception e) {
                    System.err.println("Failed to upload poster for movie " + movie.getId() + ": " + e.getMessage());
                }
            }
        }
        return count;
    }
}
