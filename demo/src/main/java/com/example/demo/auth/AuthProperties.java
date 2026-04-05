package com.example.demo.auth;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "auth.jwt")
public class AuthProperties {

    private String issuer = "algo-mind";

    private String secret = "algo-mind-demo-jwt-secret-key-change-me-2026";

    private long expirationMinutes = 60L * 24 * 7;
}
