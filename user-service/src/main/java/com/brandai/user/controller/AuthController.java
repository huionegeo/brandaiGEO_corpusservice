package com.brandai.user.controller;

import com.brandai.common.result.Result;
import com.brandai.user.dto.AuthDTO;
import com.brandai.user.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public Result<AuthDTO.TokenResponse> register(@Valid @RequestBody AuthDTO.RegisterRequest request) {
        return Result.success("注册成功", authService.register(request));
    }

    @PostMapping("/login")
    public Result<AuthDTO.TokenResponse> login(@Valid @RequestBody AuthDTO.LoginRequest request) {
        return Result.success("登录成功", authService.login(request));
    }

    @PostMapping("/refresh")
    public Result<AuthDTO.TokenResponse> refresh(@Valid @RequestBody AuthDTO.RefreshRequest request) {
        return Result.success(authService.refresh(request));
    }

    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader("X-User-Id") Long userId) {
        authService.logout(userId);
        return Result.success();
    }
}
