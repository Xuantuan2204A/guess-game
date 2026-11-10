package com.inmobi.guess_game.dto;
import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
}