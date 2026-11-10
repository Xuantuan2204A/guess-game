package com.inmobi.guess_game.controller;

import com.inmobi.guess_game.dto.ApiResponse;
import com.inmobi.guess_game.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/buy-turns")
    public ResponseEntity<ApiResponse<String>> buyTurns(Principal principal, HttpServletRequest request) {
        String username = principal.getName();
        String ipAddress = request.getRemoteAddr();
        
        String paymentUrl = paymentService.createPaymentUrl(username, ipAddress);
        
        ApiResponse<String> response = ApiResponse.<String>builder()
                .status(HttpStatus.OK.value())
                .message("Lấy URL thanh toán thành công")
                .data(paymentUrl)
                .build();
                
        return ResponseEntity.ok(response);
    }

    @GetMapping("/vnpay-ipn")
    public ResponseEntity < Map < String , String > > vnpayIpn(
            @RequestParam("vnp_TxnRef") String vnpTxnRef,
            @RequestParam("vnp_ResponseCode") String vnpResponseCode) {
        
        paymentService.processIpn(vnpTxnRef, vnpResponseCode);
        
        return ResponseEntity.ok(Map.of("RspCode", "00", "Message", "Confirm Success"));
    }
}