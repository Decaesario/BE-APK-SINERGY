package com.impal.gabungyuk.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class TokenService {

    private final String tokenSecret;

    public TokenService(@Value("${app.auth.token-secret:change-this-secret-in-production}") String tokenSecret) {
        this.tokenSecret = tokenSecret;
    }

    public String generateToken(Integer userId, long expiredAt) {
        String payload = userId + ":" + expiredAt;
        String signature = hmacSha256(payload);
        String rawToken = payload + ":" + signature;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(rawToken.getBytes(StandardCharsets.UTF_8));
    }

    public Integer extractUserIdFromAuthorizationHeader(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid Authorization header");
        }

        String token = authorizationHeader.substring(7).trim();
        return extractUserIdFromToken(token);
    }

    private Integer extractUserIdFromToken(String token) {
        try {
            String decoded = new String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8);
            String[] parts = decoded.split(":");
            if (parts.length != 3) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
            }

            int userId = Integer.parseInt(parts[0]);
            long expiredAt = Long.parseLong(parts[1]);
            String signature = parts[2];

            String payload = parts[0] + ":" + parts[1];
            String expectedSignature = hmacSha256(payload);

            if (!expectedSignature.equals(signature)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token signature");
            }

            if (System.currentTimeMillis() > expiredAt) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token has expired");
            }

            return userId;
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }
    }

    private String hmacSha256(String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(tokenSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(keySpec);
            byte[] hash = mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to sign token", exception);
        }
    }
}
