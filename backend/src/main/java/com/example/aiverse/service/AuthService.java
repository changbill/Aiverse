package com.example.aiverse.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.aiverse.common.error.ApplicationException;
import com.example.aiverse.common.error.AuthErrorCode;
import com.example.aiverse.dto.LoginRequest;
import com.example.aiverse.dto.LoginResponse;
import com.example.aiverse.dto.MeResponse;
import com.example.aiverse.dto.RegisterRequest;
import com.example.aiverse.dto.RegisterResponse;
import com.example.aiverse.entity.User;
import com.example.aiverse.entity.UserStatus;
import com.example.aiverse.repository.UserRepository;
import com.example.aiverse.security.JwtTokenProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        String nickname = request.nickname().trim();

        if (userRepository.existsByEmail(email)) {
            throw new ApplicationException(AuthErrorCode.DUPLICATE_EMAIL);
        }
        if (userRepository.existsByNickname(nickname)) {
            throw new ApplicationException(AuthErrorCode.DUPLICATE_NICKNAME);
        }

        User user = User.register(email, passwordEncoder.encode(request.password()), nickname);
        User saved = userRepository.save(user);
        return RegisterResponse.from(saved);
    }

    public LoginResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());

        // 계정 상태·비밀번호 불일치를 동일한 오류로 응답해 이메일 존재 여부를 노출하지 않는다.
        User user = userRepository.findByEmail(email)
                .filter(candidate -> candidate.getStatus() == UserStatus.ACTIVE)
                .filter(candidate -> passwordEncoder.matches(request.password(), candidate.getPassword()))
                .orElseThrow(() -> new ApplicationException(AuthErrorCode.AUTHENTICATION_FAILED));

        String accessToken = jwtTokenProvider.issueAccessToken(user.getId());
        return LoginResponse.of(accessToken, user);
    }

    public MeResponse getCurrentUser(String accessToken) {
        Long userId = jwtTokenProvider.parseUserId(accessToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(AuthErrorCode.INVALID_TOKEN));
        return MeResponse.from(user);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}
