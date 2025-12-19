package com.cbap.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * CORS configuration properties.
 */
@Component
@ConfigurationProperties(prefix = "cbap.cors")
public class CorsProperties {

    private List<String> allowedOrigins = List.of("http://localhost:3000");
    /**
     * Origin patterns (supports wildcards). Prefer this for dev when ports vary,
     * e.g. http://localhost:* .
     */
    private List<String> allowedOriginPatterns = List.of();

    /**
     * Whether to allow credentials (cookies). For Bearer-token auth this is typically false.
     */
    private boolean allowCredentials = false;

    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    public void setAllowedOrigins(List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    public List<String> getAllowedOriginPatterns() {
        return allowedOriginPatterns;
    }

    public void setAllowedOriginPatterns(List<String> allowedOriginPatterns) {
        this.allowedOriginPatterns = allowedOriginPatterns;
    }

    public boolean isAllowCredentials() {
        return allowCredentials;
    }

    public void setAllowCredentials(boolean allowCredentials) {
        this.allowCredentials = allowCredentials;
    }
}
