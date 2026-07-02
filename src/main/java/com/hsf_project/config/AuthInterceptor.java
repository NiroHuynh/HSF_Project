package com.hsf_project.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Chặn các trang yêu cầu đăng nhập: nếu session chưa có user ("ttdn")
 * thì đưa về trang đăng nhập.
 */
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("ttdn") == null) {
            response.sendRedirect(request.getContextPath() + "/");
            return false;
        }
        return true;
    }
}
