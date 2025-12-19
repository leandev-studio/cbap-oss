package com.cbap.security.filter;

import com.cbap.security.config.CorsProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Filter to handle CORS preflight (OPTIONS) requests BEFORE Spring Security processes them.
 * 
 * This ensures that OPTIONS requests get proper CORS headers even if Spring Security
 * would otherwise block them.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE - 1) // Run just before CorsFilter
public class CorsPreflightFilter extends OncePerRequestFilter {

    private final CorsProperties corsProperties;

    public CorsPreflightFilter(CorsProperties corsProperties) {
        this.corsProperties = corsProperties;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // Handle OPTIONS preflight requests immediately
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            String origin = request.getHeader("Origin");
            
            // Check if origin is allowed
            boolean isAllowed = false;
            if (origin != null) {
                if (corsProperties.getAllowedOriginPatterns() != null && !corsProperties.getAllowedOriginPatterns().isEmpty()) {
                    // Check against patterns (e.g., http://localhost:[*])
                    for (String pattern : corsProperties.getAllowedOriginPatterns()) {
                        if (matchesPattern(origin, pattern)) {
                            isAllowed = true;
                            break;
                        }
                    }
                } else if (corsProperties.getAllowedOrigins() != null && !corsProperties.getAllowedOrigins().isEmpty()) {
                    isAllowed = corsProperties.getAllowedOrigins().contains(origin);
                }
            }

            if (isAllowed || origin == null) {
                // Add CORS headers
                if (origin != null) {
                    response.setHeader("Access-Control-Allow-Origin", origin);
                }
                response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, PATCH, OPTIONS");
                response.setHeader("Access-Control-Allow-Headers", "*");
                response.setHeader("Access-Control-Expose-Headers", "Authorization, X-Correlation-ID");
                response.setHeader("Access-Control-Allow-Credentials", String.valueOf(corsProperties.isAllowCredentials()));
                response.setHeader("Access-Control-Max-Age", "3600");
            }

            // Return 200 OK for preflight
            response.setStatus(HttpServletResponse.SC_OK);
            return; // Don't continue filter chain for OPTIONS
        }

        // For non-OPTIONS requests, continue the filter chain
        filterChain.doFilter(request, response);
    }

    /**
     * Simple pattern matching for origin patterns like "http://localhost:[*]"
     */
    private boolean matchesPattern(String origin, String pattern) {
        if (pattern.contains("[*]")) {
            // Replace [*] with regex pattern
            String regex = pattern.replace("[*]", "\\d+");
            return origin.matches(regex);
        }
        return origin.equals(pattern);
    }
}
