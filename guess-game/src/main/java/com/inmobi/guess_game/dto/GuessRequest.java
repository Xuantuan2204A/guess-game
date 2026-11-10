package com.inmobi.guess_game.dto;
import lombok.Data;

@Data
public class GuessRequest {
    private int guessNumber; // Số user nhập vào (từ 1 đến 5)
}