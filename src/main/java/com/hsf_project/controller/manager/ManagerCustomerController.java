package com.hsf_project.controller.manager;

import com.hsf_project.dto.response.CustomerRowDTO;
import com.hsf_project.entity.User;
import com.hsf_project.service.CustomerService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/manager")
public class ManagerCustomerController {

    @Autowired
    private CustomerService customerService;

    @GetMapping("/customers")
    public String listCustomers(HttpSession session, Model model) {

        User user = (User) session.getAttribute("ttdn");

        // Sidebar info
        if (user != null) {
            model.addAttribute("managerName",
                    user.getLastName() + " " + user.getFirstName());
            model.addAttribute("managerEmail",
                    user.getEmail());
            model.addAttribute("managerCinemaName",
                    user.getCinema() != null ? user.getCinema().getName() : "");
        }

        // Lấy khách hàng theo cinema của staff đang đăng nhập
        List<CustomerRowDTO> customers = Collections.emptyList();

        if (user != null && user.getCinema() != null) {
            customers = customerService.getCustomersByCinema(user.getCinema().getId());
        }

        model.addAttribute("customers",  customers);
        model.addAttribute("totalCount", customers.size());
        model.addAttribute("activePage", "customers");

        return "manager/customer";
    }
}