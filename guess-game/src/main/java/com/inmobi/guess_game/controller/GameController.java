package com.inmobi.guess_game.controller;

import com.inmobi.guess_game.dto.ApiResponse;
import com.inmobi.guess_game.dto.GuessRequest;
import com.inmobi.guess_game.dto.GuessResponse;
import com.inmobi.guess_game.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/game")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    @PostMapping("/guess")
    public ResponseEntity<ApiResponse<GuessResponse>> guessNumber(
            @RequestBody GuessRequest request, 
            Principal principal) { // Principal chứa thông tin user từ token hợp lệ
        
        // Trích xuất username từ Token
        String username = principal.getName(); 
        
        GuessResponse result = gameService.play(username, request.getGuessNumber());
        
        ApiResponse<GuessResponse> response = ApiResponse.<GuessResponse>builder()
                .status(HttpStatus.OK.value())
                .message(result.isWin() ? "Bingo! Bạn đã đoán trúng." : "Rất tiếc, số của hệ thống là " + result.getServerNumber())
                .data(result)
                .build();
                
        return ResponseEntity.ok(response);
    }
}