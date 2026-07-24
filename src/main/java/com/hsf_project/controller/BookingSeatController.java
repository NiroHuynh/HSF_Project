package com.hsf_project.controller;

import com.hsf_project.dto.response.SeatPageResponseDTO;
import com.hsf_project.entity.User;
import com.hsf_project.repository.auth.UserRepository;
import com.hsf_project.service.SeatService;
import com.hsf_project.service.ShowTimeService;
import com.hsf_project.service.auth.UserService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * BƯỚC 2 trong luồng Đặt vé: Chọn ghế (dữ liệu thật từ DB).
 * Trang trước: bookingContext (chọn phim/rạp/giờ chiếu) truyền showtimeId sang.
 * Khi bấm "Tiếp tục", form POST showtimeId + seatIds (vd "A1,A2") sang
 * /booking/initiate để tạo booking PENDING giữ ghế, rồi chuyển tiếp sang bước Combo.
 */
@Controller
@RequestMapping("/booking")
public class BookingSeatController {

    @Autowired
    private ShowTimeService showTimeService;

    @Autowired
    private SeatService seatService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @PersistenceContext
    private EntityManager entityManager; //Controller quản lý bộ nhớ đệm


    @GetMapping("/seats")
    public String showSeatPage(@RequestParam Long showtimeId, Model model, HttpSession session) {

        User userSession = (User) session.getAttribute("ttdn");

        // 1. Kiểm tra Đăng nhập
        if (userSession == null) {
            session.setAttribute("redirectAfterLogin", "/seats?showtimeId=" + showtimeId);
            return "redirect:/login";
        }

        // 2. Kiểm tra phạt / khóa đặt vé (Ủy quyền hoàn toàn cho UserService)
        long bannedMinutes = userService.getRemainingLockMinutes(userSession.getId());
        if (bannedMinutes > 0) {
            return "redirect:/phim?error=banned&minutes=" + bannedMinutes;
        }

        // 3. Lấy dữ liệu hiển thị màn hình ghế (Ủy quyền hoàn toàn cho SeatService)
        SeatPageResponseDTO pageData = seatService.getSeatPageData(showtimeId);

        // 4. Đóng gói vào Model để trả về View
        model.addAttribute("showtimeId", showtimeId);
        model.addAttribute("showtime", pageData.getShowtime());
        model.addAttribute("rows", pageData.getRows());
        model.addAttribute("totalSeats", pageData.getTotalSeats());
        model.addAttribute("availableSeats", pageData.getAvailableSeats());
        model.addAttribute("seatPriceJson", pageData.getSeatPriceMap());

        return "bookingSeat";
    }
}