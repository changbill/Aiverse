package com.example.aiverse.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.aiverse.common.response.ApiResponse;
import com.example.aiverse.dto.LoginRequest;
import com.example.aiverse.dto.LoginResponse;
import com.example.aiverse.dto.MeResponse;
import com.example.aiverse.dto.ReissueResponse;
import com.example.aiverse.dto.RegisterRequest;
import com.example.aiverse.dto.RegisterResponse;
import com.example.aiverse.service.AuthService;
import com.example.aiverse.util.RefreshTokenCookieSupport;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@Tag(name = "Auth")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final long refreshTokenExpirationSeconds;

    public AuthController(
            AuthService authService,
            @Value("${app.jwt.refresh-token-expiration-seconds}") long refreshTokenExpirationSeconds
    ) {
        this.authService = authService;
        this.refreshTokenExpirationSeconds = refreshTokenExpirationSeconds;
    }

    @Operation(summary = "회원가입")
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.of(authService.register(request));
    }

    @Operation(summary = "로그인")
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletResponse httpResponse) {
        AuthService.LoginResult result = authService.login(request);
        RefreshTokenCookieSupport.addCookie(httpResponse, result.refreshToken(), refreshTokenExpirationSeconds);
        return ApiResponse.of(result.response());
    }

    @Operation(summary = "Access token 재발급 및 Refresh token 회전")
    @PostMapping("/reissue")
    public ApiResponse<ReissueResponse> reissue(
            @CookieValue(value = RefreshTokenCookieSupport.COOKIE_NAME, required = false) String refreshTokenCookie,
            HttpServletResponse httpResponse
    ) {
        AuthService.ReissueResult result = authService.reissue(refreshTokenCookie);
        RefreshTokenCookieSupport.addCookie(httpResponse, result.refreshToken(), refreshTokenExpirationSeconds);
        return ApiResponse.of(new ReissueResponse(result.accessToken()));
    }

    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(
            @CookieValue(value = RefreshTokenCookieSupport.COOKIE_NAME, required = false) String refreshTokenCookie,
            HttpServletResponse httpResponse
    ) {
        authService.logout(refreshTokenCookie);
        RefreshTokenCookieSupport.clearCookie(httpResponse);
    }

    @Operation(summary = "현재 사용자 조회")
    @SecurityRequirement(name = "bearer-jwt")
    @GetMapping("/me")
    public ApiResponse<MeResponse> me(@AuthenticationPrincipal Long userId) {
        return ApiResponse.of(authService.getCurrentUser(userId));
    }
}
