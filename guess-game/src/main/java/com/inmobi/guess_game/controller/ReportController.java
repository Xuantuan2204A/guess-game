package com.inmobi.guess_game.controller;

import com.inmobi.guess_game.dto.ApiResponse;
import com.inmobi.guess_game.dto.LeaderboardResponse;
import com.inmobi.guess_game.dto.UserInfoResponse;
import com.inmobi.guess_game.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/leaderboard")
    public ResponseEntity<ApiResponse<List<LeaderboardResponse>>> getLeaderboard() {
        List<LeaderboardResponse> data = reportService.getTop10();
        
        return ResponseEntity.ok(ApiResponse.<List<LeaderboardResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Lấy bảng xếp hạng thành công")
                .data(data)
                .build());
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserInfoResponse>> getMyInfo(Principal principal) {
        // Principal.getName() chính là Username đã được giải mã từ JWT Token
        UserInfoResponse data = reportService.getMyInfo(principal.getName());
        
        // TÁCH BIẾN RA ĐỂ TRÌNH BIÊN DỊCH DỄ HIỂU HƠN
        ApiResponse<UserInfoResponse> response = ApiResponse.<UserInfoResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Thông tin cá nhân")
                .data(data)
                .build();
                
        return ResponseEntity.ok(response);
    }
}