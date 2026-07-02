package com.hsf_project.controller.auth;

import com.hsf_project.entity.User;
import com.hsf_project.service.auth.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public String login(Model model ,HttpSession session){
        if (session.getAttribute("currentUser") != null) {
            return "redirect:/home";
        }
        User user = new User();
        model.addAttribute("user", user);
        return "login";
    }

    @PostMapping("login-submit")
    public String loginSubmit(@ModelAttribute("user") User userLogin, Model model
    , HttpSession session){
        String email = userLogin.getEmail();
        String password = userLogin.getPassword();
        User u = userService.loginByEmail(email, password);
        if(u != null) {
            session.setAttribute("ttdn", u);
            return "redirect:/home";
        }else{
            model.addAttribute("error","Username or password incorrect!!");
            return "redirect:/";
        }
    }

//    @GetMapping("/home")
//    public String home(){
//        return "home";
//    }

    @GetMapping("/logout")
    public String logout(HttpSession session){
        session.invalidate();
        return "redirect:/";
    }



}
