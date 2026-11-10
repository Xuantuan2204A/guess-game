package com.inmobi.guess_game.service;

import com.inmobi.guess_game.dto.GuessResponse;
import com.inmobi.guess_game.entity.User;
import com.inmobi.guess_game.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class GameService {
    
    private final UserRepository userRepository;
    private final StringRedisTemplate redisTemplate; // Kết nối Redis
    private final Random random = new Random();

    // @Transactional đảm bảo Data Consistency và kích hoạt Optimistic Locking
    @Transactional
    public GuessResponse play(String username, int guessNumber) {
        if (guessNumber < 1 || guessNumber > 5) {
            throw new RuntimeException("Số dự đoán không hợp lệ. Vui lòng chọn từ 1 đến 5.");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin tài khoản!"));

        if (user.getTurns() <= 0) {
            throw new RuntimeException("Bạn đã hết lượt chơi. Vui lòng mua thêm!");
        }

        boolean isWin = false;
        int serverNumber;

        // BƯỚC 2: THAO TÚNG 5% TỶ LỆ THẮNG
        int chance = random.nextInt(100) + 1; // Sinh số ngẫu nhiên từ 1 đến 100

        if (chance <= 5) {
            // Rơi vào 5%: Cho user thắng
            isWin = true;
            serverNumber = guessNumber; // Server cố tình ra số giống user
            user.setScore(user.getScore() + 1); // Cộng điểm
        } else {
            // Rơi vào 95%: Cho user thua
            List<Integer> remainingNumbers = new ArrayList<>();
            for (int i = 1; i <= 5; i++) {
                if (i != guessNumber) {
                    remainingNumbers.add(i); // Tập hợp 4 số còn lại
                }
            }
            // Server random trong 4 số không trùng với số user đoán
            serverNumber = remainingNumbers.get(random.nextInt(remainingNumbers.size()));
        }

        // Trừ 1 lượt chơi (Luôn luôn trừ dù thắng hay thua)
        user.setTurns(user.getTurns() - 1);
        
        // BƯỚC 1: XỬ LÝ CONCURRENCY
        // Nhờ cột @Version trong Entity, nếu có request spam cùng lúc, hàm save() sẽ ném lỗi ObjectOptimisticLockingFailureException
        userRepository.save(user);

        // BƯỚC 3: CẬP NHẬT LEADERBOARD TRÊN REDIS NẾU THẮNG
        if (isWin) {
            // Tăng score của user lên 1 trong ZSET có tên là 'leaderboard'
            redisTemplate.opsForZSet().incrementScore("leaderboard", user.getUsername(), 1);
        }

        return GuessResponse.builder()
                .isWin(isWin)
                .serverNumber(serverNumber)
                .currentScore(user.getScore())
                .remainingTurns(user.getTurns())
                .build();
    }
}