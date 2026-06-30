package com.hsf_project.controller.movie;

import com.hsf_project.dto.MovieHomeDTO;
import com.hsf_project.entity.MovieStatus;
import com.hsf_project.entity.Promotion;
import com.hsf_project.service.movie.HomeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {
    @Autowired
    private HomeService homeService;

    /**
     * Xử lý điều hướng khi người dùng vào trang chủ
     */
    @GetMapping({"/home"})
    public String viewHomePage(Model model) {

        // 1. Lấy danh sách phim Đang Chiếu (NOW_SHOWING) kèm chuỗi thể loại
        List<MovieHomeDTO> nowShowing = homeService.getMoviesForHome(MovieStatus.NOW_SHOWING);
        model.addAttribute("nowShowingMovies", nowShowing);

        // 2. Lấy danh sách phim Sắp Chiếu (COMING_SOON) kèm chuỗi thể loại
        List<MovieHomeDTO> comingSoon = homeService.getMoviesForHome(MovieStatus.COMING_SOON);
        model.addAttribute("comingSoonMovies", comingSoon);

        // 3. Lấy danh sách Ưu đãi độc quyền (Promotion) đang chạy
        List<Promotion> promotions = homeService.getAvailablePromotions();
        model.addAttribute("activePromotions", promotions);

        // Trả về tên file HTML giao diện (home.html nằm trong thư mục src/main/resources/templates/)
        return "home";
    }
}
