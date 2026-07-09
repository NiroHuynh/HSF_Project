package com.hsf_project.controller.manager;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/manager")
public class ManagerHomeController {

    /* ============================================================
       TRANG CHÍNH — /manager/home
       ============================================================ */
    @GetMapping("/home")
    public String home(Model model) {
        return "manager/home";
    }

    @GetMapping("/revenue")
    public String revnue(Model model) {
        return "manager/revenue";
    }
}
