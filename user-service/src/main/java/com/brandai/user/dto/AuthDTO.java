package com.brandai.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

public class AuthDTO {

    @Data
    public static class LoginRequest {
        @NotBlank(message = "用户名或邮箱不能为空")
        private String usernameOrEmail;

        @NotBlank(message = "密码不能为空")
        private String password;
    }

    @Data
    public static class RegisterRequest {
        @NotBlank(message = "用户名不能为空")
        @Size(min = 3, max = 50, message = "用户名长度 3-50 字符")
        private String username;

        @NotBlank(message = "邮箱不能为空")
        @Email(message = "邮箱格式不正确")
        private String email;

        @NotBlank(message = "密码不能为空")
        @Size(min = 8, message = "密码至少 8 位")
        private String password;

        private String fullName;
        private String phone;
    }

    @Data
    public static class TokenResponse {
        private String accessToken;
        private String refreshToken;
        private Long expiresIn;   // 秒
        private UserVO user;
    }

    @Data
    public static class RefreshRequest {
        @NotBlank(message = "refreshToken 不能为空")
        private String refreshToken;
    }

    @Data
    public static class UserVO {
        private Long id;
        private String username;
        private String email;
        private String fullName;
        private String avatarUrl;
        private String role;
    }
}
