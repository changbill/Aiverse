package com.example.aiverse.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.aiverse.common.response.ApiResponse;
import com.example.aiverse.dto.LoginRequest;
import com.example.aiverse.dto.LoginResponse;
import com.example.aiverse.dto.MeResponse;
import com.example.aiverse.dto.RegisterRequest;
import com.example.aiverse.dto.RegisterResponse;
import com.example.aiverse.service.AuthService;
import com.example.aiverse.util.BearerTokenExtractor;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Auth")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "회원가입")
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.of(authService.register(request));
    }

    @Operation(summary = "로그인")
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.of(authService.login(request));
    }

    // Security 필터가 없는 임시 구현 — 다음 이슈에서 SecurityFilterChain + @AuthenticationPrincipal로 대체되면
    // 이 헤더 파싱 코드는 제거되고 컨트롤러는 인증된 사용자 정보를 파라미터로 바로 받게 된다.
    @Operation(summary = "현재 사용자 조회")
    @SecurityRequirement(name = "bearer-jwt")
    @GetMapping("/me")
    public ApiResponse<MeResponse> me(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        String accessToken = BearerTokenExtractor.extract(authorizationHeader);
        return ApiResponse.of(authService.getCurrentUser(accessToken));
    }
}
