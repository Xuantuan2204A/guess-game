package com.inmobi.guess_game.repository;

import com.inmobi.guess_game.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Khai báo hàm tìm user theo username để phục vụ việc Đăng nhập
    Optional<User> findByUsername(String username);
    
}