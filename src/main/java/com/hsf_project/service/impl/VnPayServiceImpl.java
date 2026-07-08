package com.hsf_project.service.impl;

import com.hsf_project.service.VnPayService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;

/**
 * Tích hợp VNPay theo tài liệu Pay v2.1.0 (sandbox).
 * Không cần SDK: chỉ là query string + chữ ký HMAC-SHA512.
 * Lưu ý: VNPay ký trên chuỗi tham số ĐÃ URL-encode, sắp xếp theo alphabet.
 */
@Service
public class VnPayServiceImpl implements VnPayService {

    private static final DateTimeFormatter VNP_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    @Value("${vnpay.tmn-code}")
    private String tmnCode;

    @Value("${vnpay.hash-secret}")
    private String hashSecret;

    @Value("${vnpay.pay-url}")
    private String payUrl;

    @Value("${vnpay.return-url}")
    private String returnUrl;

    @Value("${vnpay.version}")
    private String version;

    @Override
    public String buildPaymentUrl(String bookingCode, BigDecimal finalAmount, String bankCode, String clientIp,
                                  LocalDateTime expireAt) {
        ZonedDateTime now = ZonedDateTime.now(VN_ZONE);
        // Hạn thanh toán trùng với hạn giữ ghế để VNPay không cho trả tiền khi ghế đã nhả
        ZonedDateTime expire = expireAt != null
                ? expireAt.atZone(ZoneId.systemDefault()).withZoneSameInstant(VN_ZONE)
                : now.plusMinutes(15);

        if (clientIp == null || clientIp.contains(":")) {
            clientIp = "127.0.0.1";
        }

        Map<String, String> params = new TreeMap<>();
        params.put("vnp_Version", version);
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", tmnCode);
        // VNPay yêu cầu số tiền nhân 100, không phần thập phân
        params.put("vnp_Amount", finalAmount.multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP).toPlainString());
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", bookingCode);
        params.put("vnp_OrderInfo", "Thanh toan ve xem phim " + bookingCode);
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", returnUrl);
        params.put("vnp_IpAddr", clientIp);
        params.put("vnp_CreateDate", now.format(VNP_DATE_FORMAT));
        params.put("vnp_ExpireDate", expire.format(VNP_DATE_FORMAT));
        if (bankCode != null && !bankCode.isBlank()) {
            params.put("vnp_BankCode", bankCode);
        }

        String query = buildEncodedQuery(params);
        String secureHash = hmacSha512(hashSecret, query);
        return payUrl + "?" + query + "&vnp_SecureHash=" + secureHash;
    }

    @Override
    public boolean verifyReturn(Map<String, String> allParams) {
        String receivedHash = allParams.get("vnp_SecureHash");
        if (receivedHash == null || receivedHash.isBlank()) {
            return false;
        }

        // Chỉ lấy các tham số vnp_*, loại chữ ký ra khỏi dữ liệu cần ký lại
        Map<String, String> params = new TreeMap<>();
        for (Map.Entry<String, String> e : allParams.entrySet()) {
            String key = e.getKey();
            if (key.startsWith("vnp_")
                    && !key.equals("vnp_SecureHash")
                    && !key.equals("vnp_SecureHashType")
                    && e.getValue() != null && !e.getValue().isEmpty()) {
                params.put(key, e.getValue());
            }
        }

        String expectedHash = hmacSha512(hashSecret, buildEncodedQuery(params));
        return expectedHash.equalsIgnoreCase(receivedHash);
    }

    /** Nối key=value đã URL-encode (US_ASCII) bằng dấu & — dùng chung cho cả query và chuỗi ký hash. */
    private String buildEncodedQuery(Map<String, String> sortedParams) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> e : sortedParams.entrySet()) {
            if (sb.length() > 0) {
                sb.append('&');
            }
            sb.append(URLEncoder.encode(e.getKey(), StandardCharsets.US_ASCII))
              .append('=')
              .append(URLEncoder.encode(e.getValue(), StandardCharsets.US_ASCII));
        }
        return sb.toString();
    }

    private String hmacSha512(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            byte[] bytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                hex.append(String.format("%02x", b & 0xff));
            }
            return hex.toString();
        } catch (Exception ex) {
            throw new IllegalStateException("Không tạo được chữ ký HMAC-SHA512 cho VNPay", ex);
        }
    }
}
