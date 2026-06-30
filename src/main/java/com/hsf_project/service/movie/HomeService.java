package com.hsf_project.service.movie;

import com.hsf_project.dto.MovieHomeDTO;
import com.hsf_project.entity.Movie;
import com.hsf_project.entity.MovieGenre;
import com.hsf_project.entity.MovieStatus;
import com.hsf_project.entity.Promotion;
import com.hsf_project.repository.MovieGenreRepository;
import com.hsf_project.repository.movie.MovieRepository;
import com.hsf_project.repository.promotion.PromotionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class HomeService {

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private MovieGenreRepository movieGenreRepository;

    @Autowired
    private PromotionRepository promotionRepository;

    /**
     * Lấy danh sách phim theo trạng thái và gộp các thể loại thành chuỗi bằng vòng lặp for truyền thống
     */
    public List<MovieHomeDTO> getMoviesForHome(MovieStatus status) {

        // 1. Khởi tạo danh sách kết quả chứa DTO
        List<MovieHomeDTO> resultList = new ArrayList<>();

        // 2. Khởi tạo danh sách chứa các Entity Movie sẽ lấy từ Database
        List<Movie> movieList = new ArrayList<>();

        // 3. Rẽ nhánh để gọi đúng câu Native Query dựa vào trạng thái phim
        if (status == MovieStatus.NOW_SHOWING) {

            // Lấy 4 phim Đang Chiếu điểm cao nhất.
            // Lưu ý: Phải dùng status.name() để chuyển Enum thành String cho Native Query
            movieList = movieRepository.findTopMoviesByRating(status.name());

        } else if (status == MovieStatus.COMING_SOON) {

            // Lấy ngày hiện tại
            LocalDate today = LocalDate.now();

            // Lấy 4 phim Sắp Chiếu gần ngày hiện tại nhất
            movieList = movieRepository.findUpcomingMoviesByReleaseDate(status.name(), today);

        }

        // 4. Duyệt qua từng bộ phim thu được bằng vòng lặp for-each thông thường
        for (Movie movie : movieList) {

            // Tìm các thể loại liên kết với bộ phim này
            List<MovieGenre> movieGenreList = movieGenreRepository.findByMovieId(movie.getId());

            // Dùng StringBuilder để nối chuỗi tên thể loại
            StringBuilder genresBuilder = new StringBuilder();

            // Vòng lặp for cơ bản để duyệt qua danh sách thể loại
            for (int i = 0; i < movieGenreList.size(); i++) {
                String genreName = movieGenreList.get(i).getGenre().getName();
                genresBuilder.append(genreName);

                // Nếu chưa phải phần tử cuối cùng thì thêm dấu phẩy ngăn cách
                if (i < movieGenreList.size() - 1) {
                    genresBuilder.append(", ");
                }
            }

            // Chuyển kết quả nối chuỗi thành String (Ví dụ: "Hành động, Viễn tưởng")
            String genresStr = genresBuilder.toString();

            // 5. Khởi tạo DTO chứa thông tin phim và chuỗi thể loại vừa tạo
            MovieHomeDTO dto = new MovieHomeDTO(movie, genresStr);

            // Thêm DTO vào danh sách trả về
            resultList.add(dto);
        }

        // 6. Trả về cho Controller
        return resultList;
    }

    /**
     * Lấy danh sách các ưu đãi độc quyền đang diễn ra
     */
    public List<Promotion> getAvailablePromotions() {
        return promotionRepository.findActivePromotions(LocalDateTime.now());
    }
}

