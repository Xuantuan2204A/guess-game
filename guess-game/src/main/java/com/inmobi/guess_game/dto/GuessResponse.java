package com.inmobi.guess_game.dto;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GuessResponse {
    private boolean isWin;
    private int serverNumber;
    private int currentScore;
    private int remainingTurns;
}