package com.hsf_project.config;


import com.hsf_project.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

public class RoleAuthInterceptor implements HandlerInterceptor {

    private final Set<String> allowedRoles;

    public RoleAuthInterceptor(String... allowedRoles) {
        this.allowedRoles = Set.of(allowedRoles);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        HttpSession session = request.getSession(false);
        Object attr = (session != null) ? session.getAttribute("ttdn") : null;

        if (!(attr instanceof User currentUser)) {
            session = request.getSession(true);
            session.setAttribute("redirectAfterLogin", request.getRequestURI() +
                    (request.getQueryString() != null ? "?" + request.getQueryString() : ""));
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }

        String roleName = (currentUser.getRole() == null) ? "" : currentUser.getRole().getRoleName();
        boolean allowed = allowedRoles.stream().anyMatch(r -> r.equalsIgnoreCase(roleName));

        if (!allowed) {
            response.sendRedirect(request.getContextPath() + "/access-denied");
            return false;
        }

        return true;
    }
}