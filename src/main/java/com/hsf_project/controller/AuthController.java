package com.hsf_project.controller;

import com.hsf_project.entity.User;
import com.hsf_project.service.auth.UserService;
import com.hsf_project.dto.RegisterForm;
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
        if (session.getAttribute("ttdn") != null) {
            return redirectByRole((User) session.getAttribute("ttdn"));
        }
        User user = new User();
        model.addAttribute("user", user);
        if (!model.containsAttribute("registerForm")) model.addAttribute("registerForm", new RegisterForm());
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
            String redirectUrl = (String) session.getAttribute("redirectAfterLogin");
            if (redirectUrl != null) {
                session.removeAttribute("redirectAfterLogin");
                return "redirect:" + redirectUrl;
            }
            return redirectByRole(u);
        } else {
            redirectAttributes.addFlashAttribute("error","Username or password incorrect!!");
            return "redirect:/login";
        }
    }

    @PostMapping("/dang-ky")
    public String register(@Valid @ModelAttribute("registerForm") RegisterForm form,
                           BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (!form.getPassword().equals(form.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "mismatch", "Mật khẩu xác nhận không khớp");
        }
        if (!form.isAgreeTerms()) {
            bindingResult.rejectValue("agreeTerms", "required", "Bạn cần đồng ý điều khoản sử dụng");
        }
        if (userService.emailExists(form.getEmail())) {
            bindingResult.rejectValue("email", "duplicate", "Email đã được sử dụng");
        }
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute(
                    "org.springframework.validation.BindingResult.registerForm", bindingResult);
            redirectAttributes.addFlashAttribute("registerForm", form);
            redirectAttributes.addFlashAttribute("showRegister", true);
            return "redirect:/login";
        }
        userService.registerCustomer(form);
        redirectAttributes.addFlashAttribute("success", "Đăng ký thành công. Bạn có thể đăng nhập.");
        return "redirect:/login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session){
        session.invalidate();
        return "redirect:/home"; // Đổi sang trang chủ
    }

    private String redirectByRole(User user) {
        if (user != null && user.getRole() != null) {
            String role = user.getRole().getRoleName();
            if ("ADMIN".equalsIgnoreCase(role)) return "redirect:/admin/dashboard";
            if ("MANAGER".equalsIgnoreCase(role) || "STAFF".equalsIgnoreCase(role)) return "redirect:/manager/dashboard";
        }
        return "redirect:/home";
    }
}
