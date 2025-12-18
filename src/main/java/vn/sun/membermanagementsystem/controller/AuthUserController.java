package vn.sun.membermanagementsystem.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.web.bind.annotation.*;
import vn.sun.membermanagementsystem.config.jwt.JwtUtils;
import vn.sun.membermanagementsystem.config.security.AuthenticationLoggingHandler;
import vn.sun.membermanagementsystem.config.services.CustomUserDetailsService;
import vn.sun.membermanagementsystem.dto.request.LoginRequest;
import vn.sun.membermanagementsystem.dto.response.LoginResponse;
import vn.sun.membermanagementsystem.dto.response.MessageResponse;
import vn.sun.membermanagementsystem.entities.User;


@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Authentication", description = "APIs for user authentication - login, logout")
public class AuthUserController {
    
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final CustomUserDetailsService userDetailsService;
    private final AuthenticationLoggingHandler authLoggingHandler;
    
    @Operation(
        summary = "User login",
        description = "Authenticates user with email and password, returns JWT token for subsequent API calls"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Login successful",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = LoginResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Invalid email or password",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = MessageResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = MessageResponse.class)
            )
        )
    })
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest,
                                               HttpServletRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(),
                    loginRequest.getPassword()
                )
            );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String jwt = jwtUtils.generateToken(userDetails);
            
            User user = userDetailsService.getUserByEmail(userDetails.getUsername());
            
            // Log successful API login
            authLoggingHandler.logApiLogin(user.getEmail(), request);
            
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
    
    @Operation(
        summary = "User logout",
        description = "Clears the server-side security context. Note: With JWT, the client should also delete the stored token."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Logged out successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = MessageResponse.class)
            )
        )
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(HttpServletRequest request) {
        // Get current user before clearing context
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            String email = auth.getName();
            authLoggingHandler.logApiLogout(email, request);
        }
        
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(new MessageResponse("Logged out successfully"));
    }
}
