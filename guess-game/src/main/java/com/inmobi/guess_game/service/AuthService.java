package com.inmobi.guess_game.service;

import com.inmobi.guess_game.dto.AuthResponse;
import com.inmobi.guess_game.dto.LoginRequest;
import com.inmobi.guess_game.dto.RegisterRequest;
import com.inmobi.guess_game.entity.User;
import com.inmobi.guess_game.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // Đã cấu hình @Bean ở SecurityConfig
    private final JwtService jwtService;

    public User register(RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username đã tồn tại!");
        }
        
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword())) // BĂM MẬT KHẨU
                .score(0)
                .turns(5)
                .build();
                
        return userRepository.save(user);
    }

    public AuthResponse login(LoginRequest request) {
        // 1. Tìm user
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Sai tài khoản hoặc mật khẩu!"));
                
        // 2. Kiểm tra mật khẩu (so sánh mật khẩu raw với mật khẩu đã băm trong DB)
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Sai tài khoản hoặc mật khẩu!");
        }
        
        // 3. Đúng thì sinh Token
        String token = jwtService.generateToken(user.getUsername());
        return new AuthResponse(token, user.getUsername());
    }
}