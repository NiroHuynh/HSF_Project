package com.hsf_project.controller.admin;

import com.hsf_project.dto.admin.PromotionForm;
import com.hsf_project.entity.Promotion;
import com.hsf_project.repository.PromotionRepository;
import com.hsf_project.service.PromotionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/** FE-Admin-05: CRUD Voucher (entity Promotion). Chỉ ADMIN vào được (AuthFilter chặn /admin/**). */
@Controller
@RequestMapping("/admin/vouchers")
public class AdminVoucherController {

    private static final int PAGE_SIZE = 10;

    @Autowired
    private PromotionService promotionService;

    @Autowired
    private PromotionRepository promotionRepository;

    @GetMapping
    public String list(@RequestParam(name = "kw", required = false) String keyword,
                       @RequestParam(name = "page", defaultValue = "0") int page,
                       Model model) {
        Page<Promotion> vouchers = promotionService.searchAdmin(keyword, Math.max(page, 0), PAGE_SIZE);
        model.addAttribute("vouchers", vouchers);
        model.addAttribute("kw", keyword);
        model.addAttribute("activePage", "vouchers");
        return "admin/vouchers";
    }

    /* ========================= TẠO MỚI ========================= */

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("promotionForm", new PromotionForm());
        model.addAttribute("formTitle", "Thêm Voucher mới");
        model.addAttribute("formAction", "/admin/vouchers/create");
        model.addAttribute("activePage", "vouchers");
        return "admin/voucher-form";
    }

    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("promotionForm") PromotionForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        validateBusinessRules(form, bindingResult, null);
        if (!bindingResult.hasFieldErrors("code")
                && promotionRepository.existsByCodeIgnoreCaseAndIsDeletedFalse(form.getCode().trim())) {
            bindingResult.rejectValue("code", "duplicate", "Mã voucher này đã tồn tại");
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("formTitle", "Thêm Voucher mới");
            model.addAttribute("formAction", "/admin/vouchers/create");
            model.addAttribute("activePage", "vouchers");
            return "admin/voucher-form";
        }
        Promotion created = promotionService.create(form);
        redirectAttributes.addFlashAttribute("success", "Đã tạo voucher " + created.getCode());
        return "redirect:/admin/vouchers";
    }

    /* ========================= CẬP NHẬT ========================= */

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Promotion promo = promotionService.getById(id).orElse(null);
        if (promo == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy voucher #" + id);
            return "redirect:/admin/vouchers";
        }
        PromotionForm form = new PromotionForm();
        form.setCode(promo.getCode());
        form.setName(promo.getName());
        form.setDescription(promo.getDescription());
        form.setDiscountType(promo.getDiscountType());
        form.setDiscountValue(promo.getDiscountValue());
        form.setStartDate(promo.getStartDate());
        form.setEndDate(promo.getEndDate());
        form.setUsageLimit(promo.getUsageLimit());
        form.setStatus(promo.getStatus());

        model.addAttribute("promotionForm", form);
        model.addAttribute("formTitle", "Sửa Voucher #" + id + " (" + promo.getCode() + ")");
        model.addAttribute("formAction", "/admin/vouchers/" + id + "/edit");
        model.addAttribute("activePage", "vouchers");
        return "admin/voucher-form";
    }

    @PostMapping("/{id}/edit")
    public String edit(@PathVariable Long id,
                       @Valid @ModelAttribute("promotionForm") PromotionForm form,
                       BindingResult bindingResult,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        LocalDateTime originalStart = promotionService.getById(id).map(Promotion::getStartDate).orElse(null);
        validateBusinessRules(form, bindingResult, originalStart);
        if (!bindingResult.hasFieldErrors("code")
                && promotionRepository.existsByCodeIgnoreCaseAndIsDeletedFalseAndIdNot(form.getCode().trim(), id)) {
            bindingResult.rejectValue("code", "duplicate", "Mã voucher này đã tồn tại");
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("formTitle", "Sửa Voucher #" + id);
            model.addAttribute("formAction", "/admin/vouchers/" + id + "/edit");
            model.addAttribute("activePage", "vouchers");
            return "admin/voucher-form";
        }
        promotionService.update(id, form);
        redirectAttributes.addFlashAttribute("success", "Đã cập nhật voucher #" + id);
        return "redirect:/admin/vouchers";
    }

    /* ========================= XOÁ / KHOÁ ========================= */

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        promotionService.softDelete(id);
        redirectAttributes.addFlashAttribute("success", "Đã xoá voucher #" + id);
        return "redirect:/admin/vouchers";
    }

    @PostMapping("/{id}/toggle-status")
    public String toggleStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        promotionService.toggleStatus(id);
        redirectAttributes.addFlashAttribute("success", "Đã đổi trạng thái voucher #" + id);
        return "redirect:/admin/vouchers";
    }

    /**
     * Rule nghiệp vụ vượt ra ngoài annotation: không bắt đầu trong quá khứ,
     * khoảng ngày hợp lệ, PERCENT tối đa 100.
     *
     * @param originalStart thời gian bắt đầu đang lưu trong DB (null khi tạo mới).
     *                      Voucher cũ đã chạy từ quá khứ vẫn phải sửa được các field khác,
     *                      nên chỉ chặn quá khứ khi người dùng thực sự đổi startDate.
     */
    private void validateBusinessRules(PromotionForm form, BindingResult bindingResult,
                                       LocalDateTime originalStart) {
        LocalDateTime start = form.getStartDate();
        if (start != null && start.isBefore(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES))
                && !start.equals(originalStart)) {
            bindingResult.rejectValue("startDate", "past", "Thời gian bắt đầu không được ở quá khứ");
        }
        if (form.getStartDate() != null && form.getEndDate() != null
                && !form.getEndDate().isAfter(form.getStartDate())) {
            bindingResult.rejectValue("endDate", "range", "Thời gian kết thúc phải sau thời gian bắt đầu");
        }
        if ("PERCENT".equals(form.getDiscountType()) && form.getDiscountValue() != null
                && form.getDiscountValue().compareTo(new BigDecimal("100")) > 0) {
            bindingResult.rejectValue("discountValue", "percent", "Giảm theo % không được vượt quá 100");
        }
    }
}
