package com.foodexpress.auth.model.Dto;

import com.foodexpress.common.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class AuthRequest {
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Register{
        @NotBlank(message = "name cant be blank")
        @Size(min = 2,max = 50,message ="name must be between 2 and 50 characters")
        private String name;
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;
        @NotBlank(message = "password is required")
        @Size(min = 6, max = 50, message = "password must be between 6 and 50 characters")
        private String password;

        private String phone;
        private Role Role;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Login {
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;

        @NotBlank(message = "Password is required")
        private String password;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RefreshToken {
        @NotBlank(message = "Refresh token is required")
        private String refreshToken;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChangePassword {
        @NotBlank(message = "Current password is required")
        private String currentPassword;

        @NotBlank(message = "New password is required")
        @Size(min = 6, max = 50, message = "Password must be between 6 and 50 characters")
        private String newPassword;
    }
}
