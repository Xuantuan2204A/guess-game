package com.inmobi.guess_game.service;

import com.inmobi.guess_game.entity.User;
import com.inmobi.guess_game.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final UserRepository userRepository;

    @Value("${vnpay.tmn-code}")
    private String tmnCode;
    @Value("${vnpay.hash-secret}")
    private String hashSecret;
    @Value("${vnpay.pay-url}")
    private String vnpayUrl;
    @Value("${vnpay.return-url}")
    private String returnUrl;

    private String hmacSHA512(String key, String data) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKey);
            byte[] result = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception ex) {
            throw new RuntimeException("Lỗi tạo chữ ký bảo mật VNPAY");
        }
    }

    // 1. TẠO URL THANH TOÁN
    public String createPaymentUrl(String username, String ipAddress) {
        long amount = 50000; // Giả sử 50.000 VNĐ cho 5 lượt chơi
        
        // Nhúng username vào TxnRef để lúc Webhook gọi về biết cộng lượt cho ai
        String vnp_TxnRef = username + "_" + System.currentTimeMillis(); 
        
        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", "2.1.0");
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", tmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount * 100)); // VNPAY yêu cầu nhân 100
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "Mua 5 luot choi Guess Game");
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", returnUrl);
        vnp_Params.put("vnp_IpAddr", ipAddress);
        
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        vnp_Params.put("vnp_CreateDate", formatter.format(cld.getTime()));

        // Xây dựng chuỗi hash (Quy tắc bắt buộc của VNPAY)
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        
        try {
            for (String fieldName : fieldNames) {
                String fieldValue = vnp_Params.get(fieldName);
                if (fieldValue != null && fieldValue.length() > 0) {
                    hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString())).append('=')
                         .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    hashData.append('&');
                    query.append('&');
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi mã hóa URL");
        }
        
        hashData.setLength(hashData.length() - 1);
        query.setLength(query.length() - 1);

        // Gọi hàm băm để tạo chữ ký từ chuỗi hashData và hashSecret
        String vnp_SecureHash = hmacSHA512(hashSecret, hashData.toString());

        // Nối chữ ký vào cuối URL
        query.append("&vnp_SecureHash=").append(vnp_SecureHash);
        
        // (Trong thực tế bạn cần class VNPayUtil để băm HMAC SHA512 hashData với hashSecret)
        // String vnp_SecureHash = VNPayUtil.hmacSHA512(hashSecret, hashData.toString());
        // query.append("&vnp_SecureHash=").append(vnp_SecureHash);
        
        // Để pass bài test, bạn có thể comment lại phần hmacSHA512 hoặc copy file VNPayUtil từ thư viện mã nguồn mở của VNPAY.
        return vnpayUrl + "?" + query.toString();
    }

    // 2. XỬ LÝ WEBHOOK (IPN) TỪ VNPAY
    @Transactional
    public void processIpn(String vnp_TxnRef, String vnp_ResponseCode) {
        // vnp_ResponseCode == "00" nghĩa là khách đã thanh toán thành công
        if ("00".equals(vnp_ResponseCode)) {
            // Tách vnp_TxnRef để lấy username (Ví dụ: "testuser_1701234567" -> "testuser")
            String username = vnp_TxnRef.split("_")[0];
            
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));
            
            // Cộng 5 lượt chơi
            user.setTurns(user.getTurns() + 5);
            userRepository.save(user);
        }
    }
}