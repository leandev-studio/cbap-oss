package com.cbap.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT configuration properties.
 */
@Component
@ConfigurationProperties(prefix = "cbap.security.jwt")
public class JwtProperties {

    private String secret = "cbap-oss-secret-key-change-in-production-min-256-bits";
    private long expirationMs = 86400000L; // 24 hours
    private long refreshExpirationMs = 604800000L; // 7 days

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    public void setExpirationMs(long expirationMs) {
        this.expirationMs = expirationMs;
    }

    public long getRefreshExpirationMs() {
        return refreshExpirationMs;
    }

    public void setRefreshExpirationMs(long refreshExpirationMs) {
        this.refreshExpirationMs = refreshExpirationMs;
    }
}
