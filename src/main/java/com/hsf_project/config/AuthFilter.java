package com.hsf_project.config;

import com.hsf_project.entity.User;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Filter đảm nhận cả Authentication lẫn Authorization cho toàn bộ ứng dụng.
 *
 * Nguyên tắc: mặc định mọi đường dẫn là công khai (home, movies, rạp, login,
 * register, static resources, callback VNPay /payment/vnpay-return...);
 * chỉ những prefix nằm trong PROTECTED_PATHS mới bị kiểm tra:
 *   - /admin/**   : chỉ ADMIN
 *   - /manager/** : MANAGER và ADMIN
 *   - /booking/**, /customer/**, /change-password : cần đăng nhập (mọi role)
 *
 * Chưa đăng nhập  -> lưu URL vào session "redirectAfterLogin" rồi đưa về /login.
 * Sai quyền       -> chuyển đến /access-denied.
 */
public class AuthFilter implements Filter {

    /** prefix -> danh sách role được phép; danh sách rỗng = chỉ cần đăng nhập. */
    private static final Map<String, List<String>> PROTECTED_PATHS = Map.of(
            "/admin", List.of("ADMIN"),
            "/manager", List.of("MANAGER", "ADMIN"),
            "/booking", List.of(),
            "/customer", List.of(),
            "/change-password", List.of()
    );

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String path = request.getRequestURI().substring(request.getContextPath().length());

        List<String> allowedRoles = findAllowedRoles(path);
        if (allowedRoles == null) { // đường dẫn công khai
            chain.doFilter(req, res);
            return;
        }

        HttpSession session = request.getSession(false);
        User user = (session == null) ? null : (User) session.getAttribute("ttdn");

        if (user == null) {
            // Chưa đăng nhập: nhớ lại URL đang truy cập để quay lại sau khi login
            String redirectUrl = request.getRequestURI();
            String query = request.getQueryString();
            if (query != null) {
                redirectUrl += "?" + query;
            }
            request.getSession(true).setAttribute("redirectAfterLogin", redirectUrl);
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        if (!allowedRoles.isEmpty()) {
            String roleName = (user.getRole() == null) ? "" : user.getRole().getRoleName();
            boolean permitted = allowedRoles.stream().anyMatch(r -> r.equalsIgnoreCase(roleName));
            if (!permitted) {
                response.sendRedirect(request.getContextPath() + "/access-denied");
                return;
            }
        }

        chain.doFilter(req, res);
    }

    /** Trả về danh sách role được phép cho path, hoặc null nếu path công khai. */
    private List<String> findAllowedRoles(String path) {
        for (Map.Entry<String, List<String>> entry : PROTECTED_PATHS.entrySet()) {
            String prefix = entry.getKey();
            if (path.equals(prefix) || path.startsWith(prefix + "/")) {
                return entry.getValue();
            }
        }
        return null;
    }
}
