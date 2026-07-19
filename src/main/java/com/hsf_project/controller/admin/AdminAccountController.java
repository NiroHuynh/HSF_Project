package com.hsf_project.controller.admin;

import com.hsf_project.entity.User;
import com.hsf_project.repository.CinemaRepository;
import com.hsf_project.repository.admin.AdminRoleRepository;
import com.hsf_project.repository.admin.AdminUserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/admin/accounts")
@Transactional
public class AdminAccountController {
    private final AdminUserRepository users;
    private final AdminRoleRepository roles;
    private final CinemaRepository cinemas;

    public AdminAccountController(AdminUserRepository users, AdminRoleRepository roles, CinemaRepository cinemas) {
        this.users = users; this.roles = roles; this.cinemas = cinemas;
    }

    @GetMapping
    public String accounts(Model model) {
        model.addAttribute("accounts", users.findManagers());
        model.addAttribute("cinemas", cinemas.findByIsDeletedFalseOrderByNameAsc());
        model.addAttribute("activePage", "accounts");
        return "admin/accounts";
    }

    @PostMapping("/save")
    public String save(@RequestParam(required=false) Long id, @RequestParam String firstName,
                       @RequestParam String lastName, @RequestParam String email,
                       @RequestParam(required=false) String phoneNumber,
                       @RequestParam(required=false) String password,
                       @RequestParam String status, @RequestParam Integer cinemaId,
                       RedirectAttributes flash) {
        var duplicate = users.findByEmailIgnoreCaseAndIsDeletedFalse(email.trim());
        if (duplicate.isPresent() && !duplicate.get().getId().equals(id)) {
            flash.addFlashAttribute("error", "Email đã được sử dụng.");
            return "redirect:/admin/accounts";
        }
        User user = id == null ? new User() : users.findById(id).orElseThrow();
        user.setFirstName(firstName.trim()); user.setLastName(lastName.trim());
        user.setEmail(email.trim().toLowerCase()); user.setPhoneNumber(phoneNumber);
        if (id == null) {
            if (password == null || password.length() < 6) {
                flash.addFlashAttribute("error", "Mật khẩu phải có ít nhất 6 ký tự.");
                return "redirect:/admin/accounts";
            }
            user.setPassword(password); user.setRole(roles.findByRoleNameIgnoreCaseAndIsDeletedFalse("MANAGER").orElseThrow());
            user.setCreatedAt(LocalDateTime.now()); user.setDeleted(false);
        } else if (password != null && !password.isBlank()) user.setPassword(password);
        user.setCinema(cinemas.findById(cinemaId).orElseThrow());
        user.setStatus(status); users.save(user);
        flash.addFlashAttribute("success", id == null ? "Đã thêm tài khoản manager." : "Đã cập nhật tài khoản manager.");
        return "redirect:/admin/accounts";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes flash) {
        User user = users.findById(id).orElseThrow();
        user.setDeleted(true); user.setStatus("INACTIVE"); users.save(user);
        flash.addFlashAttribute("success", "Đã xóa tài khoản manager.");
        return "redirect:/admin/accounts";
    }
}
