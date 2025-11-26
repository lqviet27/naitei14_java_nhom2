package vn.sun.membermanagementsystem.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller xử lý authentication cho Admin Web (Thymeleaf)
 * Sử dụng Session-based authentication (Stateful)
 * Endpoints:
 * - GET /admin/login - Hiển thị trang login
 * Spring Security tự động xử lý:
 * - POST /admin/login - Submit form login
 * - POST /admin/logout - Logout
 */
@Controller
@RequestMapping("/admin")
public class AuthAdminController {
    
    /**
     * Hiển thị trang login cho Admin
     * GET /admin/login
     * 
     * Spring Security tự động:
     * - Hiển thị form này khi chưa authenticated
     * - Xử lý POST /admin/login khi submit form
     * - Validate username/password
     * - Tạo session nếu thành công
     * - Redirect đến /admin/dashboard
     */
    @GetMapping("/login")
    public String loginPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model) {
        
        // Hiển thị thông báo lỗi nếu login fail
        if (error != null) {
            model.addAttribute("error", "Invalid email or password");
        }
        
        // Hiển thị thông báo logout thành công
        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully");
        }
        
        return "auth/login";
    }
    
    /**
     * Dashboard trang chủ sau khi login thành công
     * GET /admin/dashboard
     * Yêu cầu: User phải có role ADMIN và đã authenticated
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Spring Security đã check authentication và role ADMIN
        // Nếu đến được đây nghĩa là user đã login và có quyền
        return "admin/dashboard";
    }
}
