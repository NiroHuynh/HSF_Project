package com.hsf_project.controller;

import com.hsf_project.entity.Promotion;
import com.hsf_project.entity.User;
import com.hsf_project.repository.promotion.PromotionRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

@Controller
public class VoucherController {

    @Autowired
    private PromotionRepository promotionRepository;

    @GetMapping("/uu-dai")
    public String viewVouchers(HttpSession session, Model model) {
        // Tạm thời bỏ qua ép buộc đăng ký/đăng nhập để bạn test giao diện mượt mà hơn
        // Khi nào cần nộp bài, bạn có thể bỏ comment đoạn check session bên dưới ra
        /*
        User currentUser = (User) session.getAttribute("ttdn");
        if (currentUser == null) {
            return "redirect:/login";
        }
        */

        // Lấy toàn bộ voucher có trạng thái ACTIVE và chưa bị xóa
        List<Promotion> activeVouchers = promotionRepository.findByStatusAndIsDeletedFalse("ACTIVE");
        model.addAttribute("vouchers", activeVouchers);

        System.out.println(">>> Số voucher lấy được: " + activeVouchers.size());
        model.addAttribute("vouchers", activeVouchers);

        // Đánh dấu activePage để thanh điều hướng chung của nhóm tô sáng menu "Ưu đãi"
        model.addAttribute("activePage", "uu-dai");

        return "vouchers";
    }
}