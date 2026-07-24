package com.hsf_project.config;

import com.hsf_project.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

public class RoleAuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        HttpSession session = request.getSession(false);
        Object attr = (session != null) ? session.getAttribute("ttdn") : null;

        // Chưa đăng nhập -> lưu lại URL đang định vào, đá về login
        if (!(attr instanceof User currentUser)) {
            session = request.getSession(true);
            session.setAttribute("redirectAfterLogin", request.getRequestURI() +
                    (request.getQueryString() != null ? "?" + request.getQueryString() : ""));
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }

        // Module Đặt vé chỉ dành cho CUSTOMER
        String roleName = (currentUser.getRole() == null) ? "" : currentUser.getRole().getRoleName();
        if (!"CUSTOMER".equalsIgnoreCase(roleName)) {
            response.sendRedirect(request.getContextPath() + "/access-denied");
            return false;
        }

        return true;
    }
}