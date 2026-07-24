package com.hsf_project.service.impl;

import com.hsf_project.dto.MovieHomeDTO;
import com.hsf_project.dto.movie.request.CreateMovieRequest;
import com.hsf_project.dto.movie.request.UpdateMovieRequest;
import com.hsf_project.dto.movie.response.MovieResponse;
import com.hsf_project.entity.*;
import com.hsf_project.entity.enums.AgeRating;
import com.hsf_project.entity.enums.MovieStatus;
import com.hsf_project.exception.AppException;
import com.hsf_project.exception.ErrorCode;
import com.hsf_project.mapper.MovieAdminMapper;
import com.hsf_project.mapper.MovieMapper;
import com.hsf_project.repository.*;
import com.hsf_project.service.CloudinaryService;
import com.hsf_project.service.MovieService;
import com.hsf_project.service.ReviewService;
import com.hsf_project.util.VietnameseUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
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
    ReviewService reviewService;
    MovieReviewRepository movieReviewRepository;

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
        String kwNorm = VietnameseUtils.removeDiacritics(keyword);
        String[] words = kwNorm.split("\\s+");
        String titleNorm = movie.getTitle() != null ? VietnameseUtils.removeDiacritics(movie.getTitle()) : null;
        String directorNorm = movie.getDirector() != null ? VietnameseUtils.removeDiacritics(movie.getDirector()) : null;
        String castNorm = movie.getCast() != null ? VietnameseUtils.removeDiacritics(movie.getCast()) : null;
        for (String word : words) {
            if (word.isEmpty()) continue;
            if ((titleNorm != null && titleNorm.contains(word))
                    || (directorNorm != null && directorNorm.contains(word))
                    || (castNorm != null && castNorm.contains(word)))
                return true;
        }
        return false;
    }

    private boolean matchesGenreIds(Movie movie, List<Integer> genreIds) {
        if (genreIds == null || genreIds.isEmpty()) return true;
        return movie.getGenres().stream().anyMatch(g -> genreIds.contains(g.getId()));
    }

    private MovieHomeDTO convertToMovieHomeDTO(Movie movie) {
        List<String> roomTypes = showTimeRepository.findDistinctRoomTypesByMovieId(movie.getId());
        return movieMapper.toMovieHomeDTO(movie, roomTypes);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MovieHomeDTO> getMoviesByStatus(MovieStatus status, Pageable pageable) {
        return movieRepository.findByStatusAndIsDeletedFalse(status, pageable)
                .map(this::convertToMovieHomeDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MovieHomeDTO> searchMovies(String keyword, MovieStatus status, Pageable pageable) {
        String kw = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        if (kw == null) {
            return getMoviesByStatus(status, pageable);
        }
        List<Movie> movies = movieRepository.findByStatusAndIsDeletedFalse(status);
        List<Movie> filtered = movies.stream()
                .filter(m -> matchesKeyword(m, kw))
                .toList();
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filtered.size());
        List<Movie> pageContent = start < filtered.size()
                ? filtered.subList(start, end)
                : List.of();
        return new PageImpl<>(pageContent, pageable, filtered.size())
                .map(this::convertToMovieHomeDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MovieHomeDTO> getMoviesByGenreAndStatus(List<Integer> genreIds, MovieStatus status, Pageable pageable) {
        return movieRepository.findDistinctByGenres_IdInAndStatusAndIsDeletedFalse(genreIds, status, pageable)
                .map(this::convertToMovieHomeDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MovieHomeDTO> searchMoviesByGenre(List<Integer> genreIds, String keyword, MovieStatus status, Pageable pageable) {
        String kw = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        if (kw == null) {
            return getMoviesByGenreAndStatus(genreIds, status, pageable);
        }
        List<Movie> movies = movieRepository.findDistinctByGenres_IdInAndStatusAndIsDeletedFalse(genreIds, status, Pageable.unpaged()).getContent();
        List<Movie> filtered = movies.stream()
                .filter(m -> matchesKeyword(m, kw))
                .toList();
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filtered.size());
        List<Movie> pageContent = start < filtered.size()
                ? filtered.subList(start, end)
                : List.of();
        return new PageImpl<>(pageContent, pageable, filtered.size())
                .map(this::convertToMovieHomeDTO);
    }

    @Override
    @Transactional
    public MovieResponse createMovie(CreateMovieRequest request, String updatedBy) {
        if (movieRepository.existsByTitleAndReleaseYear(request.getTitle().trim(), request.getReleaseDate().getYear()))
            throw new AppException(ErrorCode.MOVIE_TITLE_YEAR_EXISTS);

        if (request.getReleaseDate().isBefore(LocalDate.now()))
            throw new AppException(ErrorCode.INVALID_RELEASE_DATE);

        if (request.getEndDate() != null && !request.getEndDate().isAfter(request.getReleaseDate()))
            throw new AppException(ErrorCode.INVALID_DATE_RANGE, "Ngày kết thúc phải sau ngày khởi chiếu");
        if (request.getEndDate() != null && request.getEndDate().isBefore(LocalDate.now()))
            throw new AppException(ErrorCode.INVALID_DATE_RANGE, "Ngày kết thúc không được trong quá khứ");

        List<Genre> genres = validateGenres(request.getGenreIds());

        Movie movie = movieAdminMapper.toMovie(request);
        movie.setStatus(request.getStatus() != null ? request.getStatus() : MovieStatus.COMING_SOON);
        movie.setAgeRating(request.getAgeRating());
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
            if (movieRepository.existsByTitleAndReleaseYear(request.getTitle().trim(), movie.getReleaseDate().getYear())) {
                if (!movie.getTitle().equalsIgnoreCase(request.getTitle().trim()))
                    throw new AppException(ErrorCode.MOVIE_TITLE_YEAR_EXISTS);
            }
        }

        if (movie.getStatus() == MovieStatus.ENDED || movie.getStatus() == MovieStatus.CANCELLED)
            throw new AppException(ErrorCode.INVALID_STATUS_TRANSITION,
                    "Không thể cập nhật phim đã " +
                    (movie.getStatus() == MovieStatus.ENDED ? "kết thúc" : "hủy"));

        boolean hasShowtimes = !showTimeRepository.findByMovieIdAndIsDeletedFalse(id).isEmpty();
        boolean hasBookings = bookingRepository.hasConfirmedBookingsByMovie(id);

        if (request.getDurationMinutes() != null && hasShowtimes)
            throw new AppException(ErrorCode.MOVIE_NOW_SHOWING, "Không thể thay đổi thời lượng khi phim đã có suất chiếu");

        if (request.getReleaseDate() != null || request.getEndDate() != null) {
            if (hasShowtimes)
                throw new AppException(ErrorCode.MOVIE_HAS_SHOWTIMES, "Không thể thay đổi ngày chiếu khi phim đã có suất chiếu");
            if (request.getReleaseDate() != null) {
                if (request.getReleaseDate().isBefore(LocalDate.now()))
                    throw new AppException(ErrorCode.INVALID_RELEASE_DATE);
            }
            if (request.getEndDate() != null) {
                LocalDate refRelease = request.getReleaseDate() != null ? request.getReleaseDate() : movie.getReleaseDate();
                if (!request.getEndDate().isAfter(refRelease))
                    throw new AppException(ErrorCode.INVALID_DATE_RANGE, "Ngày kết thúc phải sau ngày khởi chiếu");
            }
        }

        if (request.getAgeRating() != null && !request.getAgeRating().equals(movie.getAgeRating()) && hasBookings)
            throw new AppException(ErrorCode.MOVIE_HAS_BOOKINGS, "Không thể thay đổi phân loại độ tuổi khi phim đã có vé đặt");

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
                if (target == MovieStatus.NOW_SHOWING && !hasShowtimes)
                    throw new AppException(ErrorCode.NO_FUTURE_SHOWTIMES, "Không thể chuyển sang Đang chiếu khi chưa có suất chiếu");
                if (target == MovieStatus.NOW_SHOWING && movie.getPosterUrl() == null)
                    throw new AppException(ErrorCode.POSTER_REQUIRED);
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

        if (target == MovieStatus.NOW_SHOWING && movie.getPosterUrl() == null)
            throw new AppException(ErrorCode.POSTER_REQUIRED);

        if (target == MovieStatus.NOW_SHOWING
                && showTimeRepository.findByMovieIdAndIsDeletedFalse(id).isEmpty())
            throw new AppException(ErrorCode.NO_FUTURE_SHOWTIMES, "Không thể chuyển sang Đang chiếu khi chưa có suất chiếu");

        movie.setStatus(target);
        return movieAdminMapper.toMovieResponse(movieRepository.save(movie));
    }

    @Override
    @Transactional
    public MovieResponse cancelMovie(Integer id) {
        Movie movie = getMovieByIdAdmin(id);

        if (movie.getStatus() != MovieStatus.NOW_SHOWING && movie.getStatus() != MovieStatus.COMING_SOON)
            throw new AppException(ErrorCode.MOVIE_CANNOT_CANCEL);

        if (showTimeRepository.countUpcomingByMovie(id, LocalDateTime.now()) > 0)
            throw new AppException(ErrorCode.MOVIE_HAS_SHOWTIMES, "Không thể hủy phim khi còn suất chiếu trong tương lai");

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

        List<Movie> nowShowingNoEndDate = movieRepository.findByStatusAndEndDateIsNullAndIsDeletedFalse(MovieStatus.NOW_SHOWING);
        for (Movie m : nowShowingNoEndDate) {
            LocalDate defaultEndDate = m.getReleaseDate() != null ? m.getReleaseDate().plusDays(30) : today.minusDays(1);
            if (defaultEndDate.isBefore(today)) {
                m.setStatus(MovieStatus.ENDED);
            }
        }
        movieRepository.saveAll(nowShowingNoEndDate);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initRatings() {
        List<Integer> movieIds = movieReviewRepository.findDistinctMovieIdsWithReviews();
        for (Integer movieId : movieIds) {
            reviewService.recalculateRating(movieId);
        }
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
