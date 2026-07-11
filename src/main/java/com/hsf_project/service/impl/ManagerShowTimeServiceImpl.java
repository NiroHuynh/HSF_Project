package com.hsf_project.service.impl;

import com.hsf_project.dto.response.ShowTimeRowDTO;
import com.hsf_project.entity.*;
import com.hsf_project.repository.CinemaRoomRepository;
import com.hsf_project.repository.ShowTimeRepository;
import com.hsf_project.repository.TicketRepository;
import com.hsf_project.repository.movie.MovieRepository;
import com.hsf_project.service.ManagerShowTimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ManagerShowTimeServiceImpl implements ManagerShowTimeService {

    @Autowired private ShowTimeRepository    showTimeRepository;
    @Autowired private TicketRepository      ticketRepository;
    @Autowired private MovieRepository       movieRepository;
    @Autowired private CinemaRoomRepository  cinemaRoomRepository;

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter ISO_FMT  = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // ── Danh sách ─────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<ShowTimeRowDTO> getShowtimesByDate(Integer cinemaId, LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end   = date.plusDays(1).atStartOfDay();
        List<ShowTime> list = showTimeRepository.findByCinemaAndDate(cinemaId, start, end);
        if (list.isEmpty()) return Collections.emptyList();

        List<Long> ids = list.stream().map(ShowTime::getId).collect(Collectors.toList());
        Map<Long, Long> bookedMap = ticketRepository.countBookedByShowtimeIds(ids)
                .stream().collect(Collectors.toMap(
                        r -> ((Number) r[0]).longValue(),
                        r -> ((Number) r[1]).longValue()));

        return list.stream().map(st -> toDto(st, bookedMap)).collect(Collectors.toList());
    }

    private ShowTimeRowDTO toDto(ShowTime st, Map<Long, Long> bookedMap) {
        ShowTimeRowDTO dto = new ShowTimeRowDTO();
        dto.setId(st.getId());
        dto.setMovie(st.getMovie().getTitle());
        dto.setPoster(st.getMovie().getPosterUrl() != null ? st.getMovie().getPosterUrl() : "");
        dto.setAge(st.getMovie().getAgeRating() != null ? st.getMovie().getAgeRating().name() : "");
        dto.setRoom(st.getRoom().getName());
        dto.setFormat(st.getRoom().getRoomType());
        dto.setTotal(st.getRoom().getTotalSeats() != null ? st.getRoom().getTotalSeats() : 0);
        dto.setStart(st.getStartTime().format(TIME_FMT));
        dto.setEnd(st.getEndTime().format(TIME_FMT));
        dto.setDateISO(st.getStartTime().format(ISO_FMT));
        dto.setBooked(bookedMap.getOrDefault(st.getId(), 0L).intValue());
        return dto;
    }

    // ── Dropdown ──────────────────────────────────────────────────────────────

    @Override @Transactional(readOnly = true)
    public List<Movie> getAvailableMovies() {
        return movieRepository.findByStatusInForDropdown(
                List.of(MovieStatus.NOW_SHOWING, MovieStatus.COMING_SOON));
    }

    @Override @Transactional(readOnly = true)
    public List<CinemaRoom> getRoomsForCinema(Integer cinemaId) {
        return cinemaRoomRepository.findByCinemaIdAndIsDeletedFalseOrderByNameAsc(cinemaId);
    }

    // ── Thêm mới ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public String createShowtime(Integer cinemaId, Integer movieId,
                                 Integer roomId, LocalDateTime startTime) {
        String err = validateShowtime(cinemaId, movieId, roomId, startTime, null);
        if (err != null) return err;

        Movie      movie = movieRepository.findById(movieId).get();
        CinemaRoom room  = cinemaRoomRepository.findById(roomId).get();
        LocalDateTime endTime = startTime.plusMinutes(movie.getDurationMinutes());

        ShowTime st = new ShowTime();
        st.setMovie(movie); st.setRoom(room);
        st.setStartTime(startTime); st.setEndTime(endTime);
        st.setIsDeleted(false);
        showTimeRepository.save(st);
        return null;
    }

    // ── Chỉnh sửa ─────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public ShowTime getShowtimeForEdit(Long showtimeId, Integer cinemaId) {
        return showTimeRepository.findDetailById(showtimeId)
                .filter(st -> !Boolean.TRUE.equals(st.getIsDeleted()))
                .filter(st -> st.getRoom().getCinema().getId().equals(cinemaId))
                .orElse(null);
    }

    @Override
    @Transactional
    public String updateShowtime(Long showtimeId, Integer cinemaId,
                                 Integer movieId, Integer roomId, LocalDateTime startTime) {
        // Kiểm tra showtime tồn tại và thuộc cinema
        Optional<ShowTime> stOpt = showTimeRepository.findDetailById(showtimeId);
        if (stOpt.isEmpty() || Boolean.TRUE.equals(stOpt.get().getIsDeleted())) {
            return "Suất chiếu không tồn tại.";
        }
        if (!stOpt.get().getRoom().getCinema().getId().equals(cinemaId)) {
            return "Suất chiếu không thuộc chi nhánh của bạn.";
        }

        // Validate — truyền showtimeId để loại trừ chính nó khi check conflict
        String err = validateShowtime(cinemaId, movieId, roomId, startTime, showtimeId);
        if (err != null) return err;

        Movie      movie   = movieRepository.findById(movieId).get();
        CinemaRoom room    = cinemaRoomRepository.findById(roomId).get();
        LocalDateTime endTime = startTime.plusMinutes(movie.getDurationMinutes());

        ShowTime st = stOpt.get();
        st.setMovie(movie); st.setRoom(room);
        st.setStartTime(startTime); st.setEndTime(endTime);
        showTimeRepository.save(st);
        return null;
    }

    // ── Xóa ───────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public String deleteShowtime(Long showtimeId, Integer cinemaId) {
        Optional<ShowTime> stOpt = showTimeRepository.findDetailById(showtimeId);

        if (stOpt.isEmpty() || Boolean.TRUE.equals(stOpt.get().getIsDeleted())) {
            return "Suất chiếu không tồn tại.";
        }
        if (!stOpt.get().getRoom().getCinema().getId().equals(cinemaId)) {
            return "Suất chiếu không thuộc chi nhánh của bạn.";
        }

        // Kiểm tra có vé đã đặt không
        Long activeTickets = ticketRepository.countActiveTicketsByShowtimeId(showtimeId);
        if (activeTickets != null && activeTickets > 0) {
            return "Không thể xóa — suất chiếu này đã có "
                    + activeTickets + " vé được đặt.";
        }

        // Soft delete
        ShowTime st = stOpt.get();
        st.setIsDeleted(true);
        showTimeRepository.save(st);
        return null;
    }

    // ── Validate dùng chung cho tạo mới + chỉnh sửa ──────────────────────────

    /**
     * @param excludeId null khi tạo mới, showtimeId khi chỉnh sửa
     */
    private String validateShowtime(Integer cinemaId, Integer movieId,
                                    Integer roomId, LocalDateTime startTime,
                                    Long excludeId) {
        LocalDateTime now = LocalDateTime.now();

        // 1. Thời gian phải là tương lai
        if (!startTime.isAfter(now)) {
            return "Giờ bắt đầu phải là thời điểm trong tương lai.";
        }

        // 2. Không lên lịch quá 5 ngày
        if (startTime.isAfter(now.plusDays(5))) {
            return "Chỉ được tạo suất chiếu trong vòng 5 ngày tới.";
        }

        // 3. Phim phải NOW_SHOWING hoặc COMING_SOON
        Optional<Movie> movieOpt = movieRepository.findById(movieId);
        if (movieOpt.isEmpty()) return "Phim không tồn tại.";
        Movie movie = movieOpt.get();
        if (movie.getStatus() != MovieStatus.NOW_SHOWING
                && movie.getStatus() != MovieStatus.COMING_SOON) {
            return "Phim \"" + movie.getTitle() + "\" không còn được phép chiếu.";
        }

        // 4. Phòng phải thuộc cinema của manager
        Optional<CinemaRoom> roomOpt = cinemaRoomRepository.findById(roomId);
        if (roomOpt.isEmpty()) return "Phòng chiếu không tồn tại.";
        CinemaRoom room = roomOpt.get();
        if (!room.getCinema().getId().equals(cinemaId)) {
            return "Phòng chiếu không thuộc chi nhánh của bạn.";
        }
        if (Boolean.TRUE.equals(room.getIsDeleted())) {
            return "Phòng chiếu này đã bị vô hiệu hóa.";
        }

        // 5. Check trùng lịch
        LocalDateTime endTime = startTime.plusMinutes(movie.getDurationMinutes());
        Long conflicts;
        if (excludeId == null) {
            conflicts = showTimeRepository.countConflictingShowtimes(roomId, startTime, endTime);
        } else {
            conflicts = showTimeRepository.countConflictingShowtimesExcluding(
                    roomId, startTime, endTime, excludeId);
        }
        if (conflicts != null && conflicts > 0) {
            return "Phòng " + room.getName()
                    + " đã có suất chiếu trùng khung giờ này. "
                    + "Vui lòng chọn giờ khác hoặc phòng khác.";
        }

        return null; // OK
    }
}