package com.cbap.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Rate limiting configuration properties.
 */
@Component
@ConfigurationProperties(prefix = "cbap.security.rate-limit")
public class RateLimitProperties {

    private Login login = new Login();

    public Login getLogin() {
        return login;
    }

    public void setLogin(Login login) {
        this.login = login;
    }

    public static class Login {
        private int maxAttempts = 5;
        private int lockoutDurationMinutes = 30;

        public int getMaxAttempts() {
            return maxAttempts;
        }

        public void setMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }

        public int getLockoutDurationMinutes() {
            return lockoutDurationMinutes;
        }

        public void setLockoutDurationMinutes(int lockoutDurationMinutes) {
            this.lockoutDurationMinutes = lockoutDurationMinutes;
        }
    }
}
