package com.hsf_project.controller.manager;

import com.hsf_project.dto.response.ShowTimeRowDTO;
import com.hsf_project.entity.CinemaRoom;
import com.hsf_project.entity.Movie;
import com.hsf_project.entity.ShowTime;
import com.hsf_project.entity.User;
import com.hsf_project.service.ManagerShowTimeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/manager")
public class ManagerShowTimeController {

    @Autowired
    private ManagerShowTimeService managerShowTimeService;

    // ── Sidebar helper ────────────────────────────────────────────────────────
    private void addSidebar(User user, Model model) {
        if (user != null) {
            model.addAttribute("managerName",
                    user.getLastName() + " " + user.getFirstName());
            model.addAttribute("managerEmail", user.getEmail());
            model.addAttribute("managerCinemaName",
                    user.getCinema() != null ? user.getCinema().getName() : "");
        }
    }

    // ── GET /manager/showtimes ────────────────────────────────────────────────
    @GetMapping("/showtimes")
    public String showtimes(HttpSession session, Model model) {
        User user = (User) session.getAttribute("ttdn");
        addSidebar(user, model);
        model.addAttribute("activePage", "showtimes");

        LocalDate today = LocalDate.now();
        model.addAttribute("defaultDate", today.toString());

        List<ShowTimeRowDTO> showtimes = Collections.emptyList();
        if (user != null && user.getCinema() != null) {
            showtimes = managerShowTimeService.getShowtimesByDate(user.getCinema().getId(), today);
        }
        model.addAttribute("showtimes", showtimes);
        return "manager/showtime";
    }

    // ── GET /manager/showtimes/data (AJAX) ────────────────────────────────────
    @GetMapping("/showtimes/data")
    @ResponseBody
    public List<ShowTimeRowDTO> getShowtimesData(
            @RequestParam String date, HttpSession session) {
        User user = (User) session.getAttribute("ttdn");
        if (user == null || user.getCinema() == null) return Collections.emptyList();
        return managerShowTimeService
                .getShowtimesByDate(user.getCinema().getId(), LocalDate.parse(date));
    }

    // ── GET /manager/showtimes/add ────────────────────────────────────────────
    @GetMapping("/showtimes/add")
    public String showAddForm(HttpSession session, Model model) {
        User user = (User) session.getAttribute("ttdn");
        addSidebar(user, model);
        model.addAttribute("activePage", "showtimes");
        model.addAttribute("movies", managerShowTimeService.getAvailableMovies());
        model.addAttribute("rooms", user != null && user.getCinema() != null
                ? managerShowTimeService.getRoomsForCinema(user.getCinema().getId())
                : Collections.emptyList());
        model.addAttribute("error", null);
        return "manager/showtimeCreate";
    }

    // ── POST /manager/showtimes/add ───────────────────────────────────────────
    @PostMapping("/showtimes/add")
    public String handleAdd(@RequestParam Integer movieId,
                            @RequestParam Integer roomId,
                            @RequestParam String startTime,
                            HttpSession session, Model model,
                            RedirectAttributes ra) {
        User user = (User) session.getAttribute("ttdn");
        if (user == null || user.getCinema() == null) return "redirect:/login";

        String error = managerShowTimeService.createShowtime(
                user.getCinema().getId(), movieId, roomId, LocalDateTime.parse(startTime));

        if (error != null) {
            addSidebar(user, model);
            model.addAttribute("activePage",        "showtimes");
            model.addAttribute("movies",            managerShowTimeService.getAvailableMovies());
            model.addAttribute("rooms",             managerShowTimeService.getRoomsForCinema(user.getCinema().getId()));
            model.addAttribute("error",             error);
            model.addAttribute("selectedMovieId",   movieId);
            model.addAttribute("selectedRoomId",    roomId);
            model.addAttribute("selectedStartTime", startTime);
            return "manager/showtimeCreate";
        }
        ra.addFlashAttribute("success", "Thêm suất chiếu thành công!");
        return "redirect:/manager/showtimes";
    }

    // ── GET /manager/showtimes/{id}/edit ──────────────────────────────────────
    @GetMapping("/showtimes/{id}/edit")
    public String showEditForm(@PathVariable Long id,
                               HttpSession session, Model model) {
        User user = (User) session.getAttribute("ttdn");
        if (user == null || user.getCinema() == null) return "redirect:/login";

        ShowTime showtime = managerShowTimeService
                .getShowtimeForEdit(id, user.getCinema().getId());
        if (showtime == null) {
            return "redirect:/manager/showtimes";
        }

        addSidebar(user, model);
        model.addAttribute("activePage", "showtimes");
        model.addAttribute("showtime", showtime);
        model.addAttribute("movies",   managerShowTimeService.getAvailableMovies());
        model.addAttribute("rooms",    managerShowTimeService.getRoomsForCinema(user.getCinema().getId()));
        model.addAttribute("error",    null);
        return "manager/showtimeEdit";
    }

    // ── POST /manager/showtimes/{id}/edit ─────────────────────────────────────
    @PostMapping("/showtimes/{id}/edit")
    public String handleEdit(@PathVariable Long id,
                             @RequestParam Integer movieId,
                             @RequestParam Integer roomId,
                             @RequestParam String startTime,
                             HttpSession session, Model model,
                             RedirectAttributes ra) {
        User user = (User) session.getAttribute("ttdn");
        if (user == null || user.getCinema() == null) return "redirect:/login";

        String error = managerShowTimeService.updateShowtime(
                id, user.getCinema().getId(), movieId, roomId, LocalDateTime.parse(startTime));

        if (error != null) {
            ShowTime showtime = managerShowTimeService
                    .getShowtimeForEdit(id, user.getCinema().getId());
            addSidebar(user, model);
            model.addAttribute("activePage",        "showtimes");
            model.addAttribute("showtime",          showtime);
            model.addAttribute("movies",            managerShowTimeService.getAvailableMovies());
            model.addAttribute("rooms",             managerShowTimeService.getRoomsForCinema(user.getCinema().getId()));
            model.addAttribute("error",             error);
            model.addAttribute("selectedMovieId",   movieId);
            model.addAttribute("selectedRoomId",    roomId);
            model.addAttribute("selectedStartTime", startTime);
            return "manager/showtimeEdit";
        }
        ra.addFlashAttribute("success", "Cập nhật suất chiếu thành công!");
        return "redirect:/manager/showtimes";
    }

    // ── POST /manager/showtimes/{id}/delete ───────────────────────────────────
    @PostMapping("/showtimes/{id}/delete")
    public String handleDelete(@PathVariable Long id,
                               HttpSession session,
                               RedirectAttributes ra) {
        User user = (User) session.getAttribute("ttdn");
        if (user == null || user.getCinema() == null) return "redirect:/login";

        String error = managerShowTimeService.deleteShowtime(id, user.getCinema().getId());

        if (error != null) {
            ra.addFlashAttribute("error", error);
        } else {
            ra.addFlashAttribute("success", "Xóa suất chiếu thành công!");
        }
        return "redirect:/manager/showtimes";
    }
}