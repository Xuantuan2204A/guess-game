package com.inmobi.guess_game.service;

import com.inmobi.guess_game.dto.LeaderboardResponse;
import com.inmobi.guess_game.dto.UserInfoResponse;
import com.inmobi.guess_game.entity.User;
import com.inmobi.guess_game.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final StringRedisTemplate redisTemplate;
    private final UserRepository userRepository;

    // TỐI ƯU 1: Đọc Top 10 từ Redis ZSET (Độ phức tạp O(log(N)))
    public List<LeaderboardResponse> getTop10() {
        // reverseRangeWithScores tương đương lệnh ZREVRANGE leaderboard 0 9 WITHSCORES trong Redis CLI
        Set<ZSetOperations.TypedTuple<String>> topUsers = redisTemplate.opsForZSet()
                .reverseRangeWithScores("leaderboard", 0, 9);
        
        List<LeaderboardResponse> leaderboard = new ArrayList<>();
        
        if (topUsers != null) {
            for (ZSetOperations.TypedTuple<String> tuple : topUsers) {
                leaderboard.add(new LeaderboardResponse(
                        tuple.getValue(), 
                        tuple.getScore() != null ? tuple.getScore().intValue() : 0
                ));
            }
        }
        return leaderboard;
    }

    // TỐI ƯU 2: Lấy thông tin user bằng Username (đã được đánh Index tự động nhờ @Column(unique = true))
    public UserInfoResponse getMyInfo(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin tài khoản!"));
                
        return UserInfoResponse.builder()
                .username(user.getUsername())
                .score(user.getScore())
                .turns(user.getTurns())
                .build();
    }
}