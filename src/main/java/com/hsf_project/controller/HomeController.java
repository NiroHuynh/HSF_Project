package com.hsf_project.controller;

import com.hsf_project.dto.MovieHomeDTO;
import com.hsf_project.entity.enums.MovieStatus;
import com.hsf_project.entity.Promotion;
import com.hsf_project.service.HomeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private HomeService homeService;

    @GetMapping("/home")
    public String viewHomePage(Model model) {
        List<MovieHomeDTO> nowShowing = homeService.getMoviesForHome(MovieStatus.NOW_SHOWING);
        model.addAttribute("nowShowingMovies", nowShowing);

        List<MovieHomeDTO> comingSoon = homeService.getMoviesForHome(MovieStatus.COMING_SOON);
        model.addAttribute("comingSoonMovies", comingSoon);

        List<Promotion> promotions = homeService.getAvailablePromotions();
        model.addAttribute("activePromotions", promotions);

        return "home";
    }
}