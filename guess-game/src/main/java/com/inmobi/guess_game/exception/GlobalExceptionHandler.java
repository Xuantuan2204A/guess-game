package com.inmobi.guess_game.exception;

import com.inmobi.guess_game.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.springframework.orm.ObjectOptimisticLockingFailureException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Object>> handleRuntimeException(RuntimeException ex) {
        ApiResponse<Object> response = ApiResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value()) // Trả về mã 400
                .message(ex.getMessage()) // Lấy câu thông báo lỗi ("Hết lượt chơi", "Username đã tồn tại"...)
                .data(null) // Data bằng null khi có lỗi
                .build();
                
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // Bắt các lỗi hệ thống không lường trước được (NullPointerException, lỗi SQL...)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGeneralException(Exception ex) {
        ApiResponse<Object> response = ApiResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value()) // Trả về mã 500
                .message("Đã xảy ra lỗi hệ thống: " + ex.getMessage())
                .data(null)
                .build();
                
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    // Bắt lỗi khi User cố tình gọi API cùng lúc bằng Tool (Race Condition)
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ApiResponse<Object>> handleOptimisticLocking(ObjectOptimisticLockingFailureException ex) {
        ApiResponse<Object> response = ApiResponse.builder()
                .status(HttpStatus.CONFLICT.value()) // Mã 409
                .message("Phát hiện thao tác quá nhanh! Vui lòng không spam request.")
                .data(null)
                .build();
                
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }
}