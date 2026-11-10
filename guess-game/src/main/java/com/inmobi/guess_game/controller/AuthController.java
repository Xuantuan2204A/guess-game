package com.inmobi.guess_game.controller;

import com.inmobi.guess_game.dto.ApiResponse;
import com.inmobi.guess_game.dto.AuthResponse;
import com.inmobi.guess_game.dto.LoginRequest;
import com.inmobi.guess_game.dto.RegisterRequest;
import com.inmobi.guess_game.entity.User;
import com.inmobi.guess_game.repository.UserRepository; // Import Repository của bạn
import com.inmobi.guess_game.service.AuthService;
import com.inmobi.guess_game.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    
    // Đã thay thế UserDetailsService bằng UserRepository
    private final UserRepository userRepository; 

    @PostMapping("/register")
    public ResponseEntity < ApiResponse < User > > register(@RequestBody RegisterRequest request) {
        User newUser = authService.register(request);
        
        ApiResponse < User > response = ApiResponse . < User > builder()
                .status(HttpStatus.OK.value())
                .message("Đăng ký thành công")
                .data(newUser)
                .build();
                
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity < ApiResponse < AuthResponse > > login(@RequestBody LoginRequest request) {
        
        // 1. Chạy luồng xác thực cũ (AuthService của bạn xử lý)
        AuthResponse authData = authService.login(request);
        
        // 2. Tự móc thẳng vào MySQL qua UserRepository để lấy thông tin tạo Cookie
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user trong Database"));
        
        String refreshToken = jwtService.generateRefreshToken(user);
        
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false) // localhost
                .path("/")
                .maxAge(7 * 24 * 60 * 60)
                .sameSite("Lax")
                .build();
        
        ApiResponse < AuthResponse > response = ApiResponse . < AuthResponse > builder()
                .status(HttpStatus.OK.value())
                .message("Đăng nhập thành công")
                .data(authData)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity < ApiResponse < Map < String, String > > > refresh(
            @CookieValue(name = "refreshToken", required = false) String refreshToken) {
        
        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        String username = jwtService.extractUsername(refreshToken);
        
        // Dùng UserRepository để tìm User khi Token hết hạn
        User user = userRepository.findByUsername(username).orElse(null);

        if (user != null && jwtService.isTokenValid(refreshToken, user)) {
            
            String newAccessToken = jwtService.generateToken(user);
            
            ApiResponse < Map < String, String > > response = ApiResponse . < Map < String , String > > builder()
                    .status(HttpStatus.OK.value())
                    .message("Refresh thành công")
                    .data(Map.of("token", newAccessToken))
                    .build();
                    
            return ResponseEntity.ok(response);
        }
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
    }
}