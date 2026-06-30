package com.hsf_project.controller;

import com.hsf_project.entity.Combo;
import com.hsf_project.service.BookingComboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/booking")
public class BookingComboController {

    @Autowired
    private BookingComboService bookingComboService;

    @GetMapping("/combo")
    public String showComboPage(
            @RequestParam Long showtimeId,
            @RequestParam String seatIds,
            @RequestParam(defaultValue = "0") BigDecimal seatTotal,
            Model model) {

        List<String> selectedSeats = Arrays.stream(seatIds.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        List<Combo> combos = bookingComboService.getActiveCombos();

        model.addAttribute("showtimeId", showtimeId);
        model.addAttribute("seatIds", seatIds);
        model.addAttribute("selectedSeats", selectedSeats);
        model.addAttribute("seatTotal", seatTotal);
        model.addAttribute("combos", combos);

        return "bookingCombo";
    }
}