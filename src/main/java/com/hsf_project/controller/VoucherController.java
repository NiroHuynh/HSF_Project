package com.hsf_project.controller;

import com.hsf_project.entity.Promotion;
import com.hsf_project.repository.PromotionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/customer")
public class VoucherController {

    @Autowired
    private PromotionRepository promotionRepository;

    @GetMapping("/vouchers")
    public String viewVouchers(Model model) {
        // Lấy danh sách voucher hợp lệ tại thời điểm hiện tại
        List<Promotion> activeVouchers = promotionRepository.findActivePromotions(LocalDateTime.now());
        model.addAttribute("vouchers", activeVouchers);
        model.addAttribute("activePage", "uu-dai");
        return "vouchers";
    }
}