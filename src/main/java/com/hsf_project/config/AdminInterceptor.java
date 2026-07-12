package com.hsf_project.config;

import com.hsf_project.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

public class AdminInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("ttdn") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write("{\"code\":1017,\"message\":\"Vui lòng đăng nhập\",\"data\":null}");
            return false;
        }

        User user = (User) session.getAttribute("ttdn");
        String roleName = user.getRole().getRoleName();
        if (!"ADMIN".equalsIgnoreCase(roleName) && !"MANAGER".equalsIgnoreCase(roleName)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write("{\"code\":1016,\"message\":\"Bạn không có quyền thực hiện hành động này\",\"data\":null}");
            return false;
        }

        return true;
    }
}
