package com.cbap.api.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for generating a password reset token.
 */
public class PasswordResetTokenRequest {

    @NotBlank(message = "Username or email is required")
    private String usernameOrEmail;

    public PasswordResetTokenRequest() {
    }

    public PasswordResetTokenRequest(String usernameOrEmail) {
        this.usernameOrEmail = usernameOrEmail;
    }

    public String getUsernameOrEmail() {
        return usernameOrEmail;
    }

    public void setUsernameOrEmail(String usernameOrEmail) {
        this.usernameOrEmail = usernameOrEmail;
    }
}
