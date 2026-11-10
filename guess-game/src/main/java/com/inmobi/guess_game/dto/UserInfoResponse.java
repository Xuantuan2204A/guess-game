package com.inmobi.guess_game.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserInfoResponse {
    private String username;
    private int score;
    private int turns;
}