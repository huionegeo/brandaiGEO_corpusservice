package com.brandai.user.service;

import com.brandai.common.exception.BusinessException;
import com.brandai.common.result.ResultCode;
import com.brandai.common.util.JwtUtil;
import com.brandai.user.domain.User;
import com.brandai.user.dto.AuthDTO;
import com.brandai.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

/**
 * 用户认证服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate redisTemplate;

    @Value("${jwt.secret}")
    private String jwtSecret;

    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
    private static final long REFRESH_TOKEN_TTL_DAYS = 7;

    @Transactional
    public AuthDTO.TokenResponse register(AuthDTO.RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(ResultCode.CONFLICT.getCode(), "用户名已被占用");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ResultCode.CONFLICT.getCode(), "邮箱已被注册");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user = userRepository.save(user);

        log.info("新用户注册成功: id={}, username={}", user.getId(), user.getUsername());
        return buildTokenResponse(user);
    }

    public AuthDTO.TokenResponse login(AuthDTO.LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsernameOrEmail())
                .or(() -> userRepository.findByEmail(request.getUsernameOrEmail()))
                .orElseThrow(() -> new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "用户名或密码错误"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "用户名或密码错误");
        }

        if (user.getStatus() != User.UserStatus.ACTIVE) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "账号已被禁用，请联系管理员");
        }

        log.info("用户登录成功: id={}, username={}", user.getId(), user.getUsername());
        return buildTokenResponse(user);
    }

    public AuthDTO.TokenResponse refresh(AuthDTO.RefreshRequest request) {
        JwtUtil jwtUtil = new JwtUtil(jwtSecret);
        if (!jwtUtil.isTokenValid(request.getRefreshToken())) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "刷新 Token 无效或已过期");
        }

        Long userId = jwtUtil.getUserId(request.getRefreshToken());
        // 校验 Redis 中存储的 refreshToken
        String storedToken = redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + userId);
        if (!request.getRefreshToken().equals(storedToken)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "刷新 Token 已失效");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND));
        return buildTokenResponse(user);
    }

    public void logout(Long userId) {
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + userId);
        log.info("用户已登出: id={}", userId);
    }

    private AuthDTO.TokenResponse buildTokenResponse(User user) {
        JwtUtil jwtUtil = new JwtUtil(jwtSecret);
        String accessToken = jwtUtil.generateAccessToken(
                user.getId(), user.getUsername(), user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        // 将 refreshToken 存入 Redis
        redisTemplate.opsForValue().set(
                REFRESH_TOKEN_PREFIX + user.getId(),
                refreshToken,
                Duration.ofDays(REFRESH_TOKEN_TTL_DAYS)
        );

        AuthDTO.UserVO userVO = new AuthDTO.UserVO();
        userVO.setId(user.getId());
        userVO.setUsername(user.getUsername());
        userVO.setEmail(user.getEmail());
        userVO.setFullName(user.getFullName());
        userVO.setAvatarUrl(user.getAvatarUrl());
        userVO.setRole(user.getRole().name());

        AuthDTO.TokenResponse response = new AuthDTO.TokenResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setExpiresIn(7200L);
        response.setUser(userVO);
        return response;
    }
}
