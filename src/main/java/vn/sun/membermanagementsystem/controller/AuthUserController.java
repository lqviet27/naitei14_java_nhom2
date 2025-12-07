package vn.sun.membermanagementsystem.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import vn.sun.membermanagementsystem.config.jwt.JwtUtils;
import vn.sun.membermanagementsystem.config.services.CustomUserDetailsService;
import vn.sun.membermanagementsystem.dto.request.LoginRequest;
import vn.sun.membermanagementsystem.dto.request.RegisterRequest;
import vn.sun.membermanagementsystem.dto.response.LoginResponse;
import vn.sun.membermanagementsystem.dto.response.MessageResponse;
import vn.sun.membermanagementsystem.dto.response.UserListItemDTO;
import vn.sun.membermanagementsystem.entities.User;
import vn.sun.membermanagementsystem.enums.UserRole;
import vn.sun.membermanagementsystem.enums.UserStatus;
import vn.sun.membermanagementsystem.repositories.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controller xử lý authentication cho API (Client/Mobile)
 * Sử dụng JWT token (Stateless)
 * Endpoints:
 * - POST /api/v1/auth/login - Đăng nhập
 * - POST /api/v1/auth/register - Đăng ký
 * - GET /api/v1/auth/profile - Lấy thông tin user hiện tại
 * - POST /api/v1/auth/logout - Đăng xuất (client side)
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthUserController {
    
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final CustomUserDetailsService userDetailsService;
    
    /**
     * API Login - Trả về JWT token
     * POST /api/v1/auth/login
     * Body: {"email": "user@example.com", "password": "password123"}
     * Response: {"token": "eyJ...", "type": "Bearer", "email": "...", "role": "USER", "userId": 1}
     */
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            // 1. Xác thực email/password bằng AuthenticationManager
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(),
                    loginRequest.getPassword()
                )
            );
            
            // 2. Set authentication vào SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // 3. Generate JWT token từ UserDetails
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String jwt = jwtUtils.generateToken(userDetails);
            
            // 4. Lấy thông tin user từ DB
            User user = userDetailsService.getUserByEmail(userDetails.getUsername());
            
            // 5. Trả về response với token và thông tin user
            LoginResponse response = LoginResponse.builder()
                .token(jwt)
                .email(user.getEmail())
                .role(user.getRole().toString())
                .userId(user.getId())
                .build();
            
            return ResponseEntity.ok(response);
            
        } catch (BadCredentialsException e) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new MessageResponse("Invalid email or password", false));
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("An error occurred during login: " + e.getMessage(), false));
        }
    }
    
    /**
     * API Register - Đăng ký user mới
     * POST /api/v1/auth/register
     * Body: {"name": "John", "email": "john@example.com", "password": "password123"}
     * Response: {"message": "User registered successfully", "success": true}
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            // 1. Kiểm tra email đã tồn tại chưa
            if (userRepository.existsByEmailAndNotDeleted(registerRequest.getEmail())) {
                return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Email is already in use", false));
            }
            
            // 2. Tạo user mới
            User user = User.builder()
                .name(registerRequest.getName())
                .email(registerRequest.getEmail())
                .passwordHash(passwordEncoder.encode(registerRequest.getPassword()))
                .role(UserRole.MEMBER) // Default role
                .status(UserStatus.ACTIVE) // Default status
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
            
            // 3. Lưu vào database
            userRepository.save(user);
            
            return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new MessageResponse("User registered successfully"));
            
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("An error occurred during registration: " + e.getMessage(), false));
        }
    }
    
    /**
     * API Register Admin - CHỈ DÙNG ĐỂ TẠO ADMIN ACCOUNT ĐẦU TIÊN
     * POST /api/v1/auth/register-admin
     * Body: {"name": "Admin", "email": "admin@example.com", "password": "admin123"}
     * 
     * ⚠️ SAU KHI TẠO ADMIN, NÊN XÓA HOẶC BẢO MẬT ENDPOINT NÀY
     */
    /*
    @PostMapping("/register-admin")
    public ResponseEntity<?> registerAdmin(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            // ⚠️ CHỈ CHO PHÉP TẠO ADMIN NẾU CHƯA CÓ ADMIN NÀO
            List<User> existingAdmins = userRepository.findByRoleAndNotDeleted(UserRole.ADMIN);
            if (!existingAdmins.isEmpty()) {
                return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("Admin account already exists. Registration is disabled.", false));
            }
            
            // Kiểm tra email đã tồn tại chưa
            if (userRepository.existsByEmailAndNotDeleted(registerRequest.getEmail())) {
                return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Email is already in use", false));
            }
            
            // Tạo admin user
            User admin = User.builder()
                .name(registerRequest.getName())
                .email(registerRequest.getEmail())
                .passwordHash(passwordEncoder.encode(registerRequest.getPassword()))
                .role(UserRole.ADMIN) // ⭐ Set role là ADMIN
                .status(UserStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
            
            userRepository.save(admin);
            
            return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new MessageResponse("Admin registered successfully! Please login with your credentials."));
            
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("An error occurred during admin registration: " + e.getMessage(), false));
        }
    }
    */
    
    /**
     * API Get current user profile
     * GET /api/v1/auth/profile
     * Header: Authorization: Bearer <token>
     * Response: User object
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new MessageResponse("Unauthorized", false));
        }
        
        try {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userDetailsService.getUserByEmail(userDetails.getUsername());
            
            // ⭐ Map sang DTO không có passwordHash
            UserListItemDTO profile = UserListItemDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .birthday(user.getBirthday())
                .role(user.getRole())
                .status(user.getStatus())
                .build();
            
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("Error fetching user profile: " + e.getMessage(), false));
        }
    }
    
    /**
     * API Logout (Optional - Client side xóa token)
     * POST /api/v1/auth/logout
     * Note: Với JWT, logout thường xử lý ở client (xóa token)
     * Server side chỉ clear SecurityContext
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(new MessageResponse("Logged out successfully"));
    }
}
