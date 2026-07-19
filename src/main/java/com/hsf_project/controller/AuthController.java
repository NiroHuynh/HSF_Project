package com.hsf_project.controller;

import com.hsf_project.dto.auth.ChangePasswordForm;
import com.hsf_project.dto.auth.ForgotPasswordForm;
import com.hsf_project.dto.auth.RegisterForm;
import com.hsf_project.entity.User;
import com.hsf_project.service.auth.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public String root(HttpSession session){
        return "redirect:/home";
    }

    @GetMapping("/login")
    public String login(Model model, HttpSession session){
        if (session.getAttribute("ttdn") instanceof User currentUser) {
            return redirectByRole(currentUser, session);
        }
        model.addAttribute("user", new User());
        if (!model.containsAttribute("registerForm")) {
            model.addAttribute("registerForm", new RegisterForm());
        }
        return "login";
    }

    @PostMapping("/login-submit")
    public String loginSubmit(@ModelAttribute("user") User userLogin, RedirectAttributes redirectAttributes
            , HttpSession session){
        String email = userLogin.getEmail();
        String password = userLogin.getPassword();
        User u = userService.loginByEmail(email, password);
        if(u != null) {
            session.setAttribute("ttdn", u);
            return redirectByRole(u, session);
        } else {
            redirectAttributes.addFlashAttribute("error","Username or password incorrect!!");
            return "redirect:/login";
        }
    }

    /**
     * Điều hướng sau khi đăng nhập: ADMIN -> /admin, MANAGER -> /manager/dashboard
     * (cả hai bỏ qua redirectAfterLogin), còn lại -> URL đã lưu trước đó hoặc /home.
     *
     * Tài khoản quản trị đăng nhập là để làm việc trong khu quản trị, nên đưa thẳng
     * vào đó thay vì thả về trang chủ rồi bắt tự bấm link trên header.
     */
    private String redirectByRole(User user, HttpSession session) {
        String roleName = (user.getRole() == null) ? "" : user.getRole().getRoleName();
        if ("ADMIN".equalsIgnoreCase(roleName)) {
            session.removeAttribute("redirectAfterLogin");
            return "redirect:/admin";
        }
        if ("MANAGER".equalsIgnoreCase(roleName)) {
            session.removeAttribute("redirectAfterLogin");
            return "redirect:/manager/dashboard";
        }
        String redirectUrl = (String) session.getAttribute("redirectAfterLogin");
        if (redirectUrl != null) {
            session.removeAttribute("redirectAfterLogin");
            return "redirect:" + redirectUrl;
        }
        return "redirect:/home";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session){
        session.invalidate();
        return "redirect:/home"; // Đổi sang trang chủ
    }

    /* ============================ ĐĂNG KÝ ============================ */

    @GetMapping("/register")
    public String register(HttpSession session){
        if (session.getAttribute("ttdn") != null) {
            return "redirect:/home";
        }
        // Form đăng ký nằm chung trang login (tab thứ hai)
        return "redirect:/login";
    }

    @PostMapping("/register")
    public String registerSubmit(@Valid @ModelAttribute("registerForm") RegisterForm form,
                                 BindingResult bindingResult,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (!bindingResult.hasFieldErrors("confirmPassword")
                && form.getPassword() != null && !form.getPassword().equals(form.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "mismatch", "Mật khẩu nhập lại không khớp");
        }
        // Số điện thoại là thông tin liên hệ để rạp gọi khi có sự cố với vé, nên không
        // cho hai tài khoản dùng chung một số.
        if (!bindingResult.hasFieldErrors("phoneNumber") && userService.isPhoneTaken(form.getPhoneNumber())) {
            bindingResult.rejectValue("phoneNumber", "duplicate", "Số điện thoại này đã được đăng ký");
        }
        if (!bindingResult.hasErrors()) {
            User created = userService.register(form);
            if (created == null) {
                bindingResult.rejectValue("email", "duplicate", "Email này đã được đăng ký");
            } else {
                redirectAttributes.addFlashAttribute("success",
                        "Đăng ký thành công! Hãy đăng nhập bằng tài khoản vừa tạo.");
                return "redirect:/login";
            }
        }
        // Có lỗi: render lại trang login với tab Đăng ký đang mở
        model.addAttribute("user", new User());
        model.addAttribute("showRegister", true);
        return "login";
    }

    /* ========================= QUÊN MẬT KHẨU ========================= */

    @GetMapping("/forgot-password")
    public String forgotPassword(Model model, HttpSession session) {
        if (session.getAttribute("ttdn") != null) {
            return "redirect:/home";
        }
        model.addAttribute("forgotForm", new ForgotPasswordForm());
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPasswordSubmit(@Valid @ModelAttribute("forgotForm") ForgotPasswordForm form,
                                       BindingResult bindingResult,
                                       RedirectAttributes redirectAttributes) {
        if (!bindingResult.hasFieldErrors("confirmPassword")
                && form.getNewPassword() != null && !form.getNewPassword().equals(form.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "mismatch", "Mật khẩu nhập lại không khớp");
        }
        if (!bindingResult.hasErrors() && !userService.resetPassword(form.getEmail(), form.getNewPassword())) {
            bindingResult.rejectValue("email", "notfound", "Không tìm thấy tài khoản với email này");
        }
        if (bindingResult.hasErrors()) {
            return "forgot-password";
        }
        redirectAttributes.addFlashAttribute("success",
                "Đặt lại mật khẩu thành công! Hãy đăng nhập bằng mật khẩu mới.");
        return "redirect:/login";
    }

    /* ========================== ĐỔI MẬT KHẨU ========================== */

    @GetMapping("/change-password")
    public String changePassword(Model model) {
        model.addAttribute("changeForm", new ChangePasswordForm());
        return "change-password";
    }

    @PostMapping("/change-password")
    public String changePasswordSubmit(@Valid @ModelAttribute("changeForm") ChangePasswordForm form,
                                       BindingResult bindingResult,
                                       HttpSession session,
                                       RedirectAttributes redirectAttributes) {
        User current = (User) session.getAttribute("ttdn");
        if (current == null) {
            return "redirect:/login";
        }
        if (!bindingResult.hasFieldErrors("confirmPassword")
                && form.getNewPassword() != null && !form.getNewPassword().equals(form.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "mismatch", "Mật khẩu nhập lại không khớp");
        }
        if (!bindingResult.hasErrors()) {
            User updated = userService.changePassword(current.getId(), form.getOldPassword(), form.getNewPassword());
            if (updated == null) {
                bindingResult.rejectValue("oldPassword", "wrong", "Mật khẩu hiện tại không đúng");
            } else {
                session.setAttribute("ttdn", updated); // làm mới user trong session
                redirectAttributes.addFlashAttribute("success", "Đổi mật khẩu thành công!");
                return "redirect:/change-password";
            }
        }
        return "change-password";
    }

    /* ========================== TRANG CHẶN QUYỀN ========================== */

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied";
    }
}
