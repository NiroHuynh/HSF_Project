package com.hsf_project.controller.admin;

import com.hsf_project.entity.Promotion;
import com.hsf_project.service.admin.AdminCatalogService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/vouchers")
public class AdminVoucherController {
    private final AdminCatalogService catalog;

    public AdminVoucherController(AdminCatalogService catalog) {
        this.catalog = catalog;
    }

    @GetMapping
    public String list(@RequestParam(required = false) Long edit, Model model) {
        model.addAttribute("vouchers", catalog.getPromotions());
        model.addAttribute("voucher", edit == null ? new Promotion() : catalog.getPromotion(edit));
        model.addAttribute("activePage", "vouchers");
        return "admin/vouchers";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Promotion voucher, RedirectAttributes flash) {
        try {
            catalog.savePromotion(voucher);
            flash.addFlashAttribute("success", voucher.getId() == null ? "Đã thêm voucher." : "Đã cập nhật voucher.");
        } catch (RuntimeException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/vouchers";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes flash) {
        catalog.deletePromotion(id);
        flash.addFlashAttribute("success", "Đã ẩn voucher.");
        return "redirect:/admin/vouchers";
    }
}
