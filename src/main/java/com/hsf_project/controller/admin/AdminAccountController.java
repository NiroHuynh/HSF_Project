package com.hsf_project.controller.admin;

import com.hsf_project.dto.admin.response.AdminAccountResponse;
import com.hsf_project.repository.CinemaRepository;
import com.hsf_project.service.admin.AdminAccountService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Chỉ render trang quản lý tài khoản. Các endpoint JSON (tạo/sửa/xóa/khóa) nằm ở
 * {@link AdminAccountRestController} vì chúng cần GlobalRestExceptionHandler trả lỗi
 * dạng JSON — advice đó chỉ áp dụng cho @RestController.
 */
@Controller
@RequestMapping("/admin/accounts")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class AdminAccountController {

    AdminAccountService adminAccountService;
    CinemaRepository cinemaRepository;

    @GetMapping
    public String pageAccounts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "ALL") String role,
            Model model) {

        List<AdminAccountResponse> accounts = adminAccountService.getAccounts(keyword, role);
        model.addAttribute("accounts", accounts);
        model.addAttribute("totalCount", adminAccountService.getTotalCount());
        model.addAttribute("activeCount", adminAccountService.getActiveCount());
        model.addAttribute("lockedCount", adminAccountService.getLockedCount());
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedRole", role);
        model.addAttribute("cinemas", cinemaRepository.findByIsDeletedFalseOrderByNameAsc());
        model.addAttribute("active", "accounts");

        return "adminManagement";
    }
}
