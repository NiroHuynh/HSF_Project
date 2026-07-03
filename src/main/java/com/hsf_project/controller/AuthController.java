package com.hsf_project.controller;

import com.hsf_project.entity.User;
import com.hsf_project.service.auth.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    /**
     * SỬA: Khi vừa vào localhost:8080 sẽ chuyển hướng thẳng sang trang chủ /home
     */
    @GetMapping("/")
    public String root(HttpSession session){
        return "redirect:/home";
    }

    /**
     * SỬA: Nếu người dùng đã đăng nhập rồi mà cố tình vào lại /login,
     * chúng ta sẽ đá họ về trang chủ /home luôn
     */
    @GetMapping("/login")
    public String login(Model model, HttpSession session){
        if (session.getAttribute("ttdn") != null) {
            return "redirect:/home";
        }
        User user = new User();
        model.addAttribute("user", user);
        return "login";
    }

    /**
     * SỬA: Sau khi đăng nhập thành công (và không có yêu cầu chuyển hướng ngầm trước đó),
     * hệ thống sẽ đưa người dùng về trang chủ /home thay vì /phim
     */
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
            return "redirect:/home"; // Đổi sang trang chủ
        } else {
            redirectAttributes.addFlashAttribute("error","Username or password incorrect!!");
            return "redirect:/login";
        }
    }

    /**
     * SỬA: Sau khi người dùng nhấn Đăng xuất (Logout), hủy session
     * và đưa họ quay về màn hình trang chủ /home cho sạch sẽ
     */
    @GetMapping("/logout")
    public String logout(HttpSession session){
        session.invalidate();
        return "redirect:/home"; // Đổi sang trang chủ
    }
}