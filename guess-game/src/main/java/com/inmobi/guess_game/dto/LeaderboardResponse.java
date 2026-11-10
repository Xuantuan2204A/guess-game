package com.inmobi.guess_game.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LeaderboardResponse {
    private String username;
    private int score;
}