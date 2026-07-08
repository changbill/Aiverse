package com.example.aiverse.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.aiverse.common.error.ApplicationException;
import com.example.aiverse.common.error.AuthErrorCode;
import com.example.aiverse.dto.LoginRequest;
import com.example.aiverse.dto.LoginResponse;
import com.example.aiverse.dto.MeResponse;
import com.example.aiverse.dto.MeUpdateRequest;
import com.example.aiverse.dto.RegisterRequest;
import com.example.aiverse.dto.RegisterResponse;
import com.example.aiverse.entity.RefreshToken;
import com.example.aiverse.entity.User;
import com.example.aiverse.entity.UserStatus;
import com.example.aiverse.repository.RefreshTokenRepository;
import com.example.aiverse.repository.UserRepository;
import com.example.aiverse.security.JwtTokenProvider;
import com.example.aiverse.security.RefreshTokenGenerator;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenGenerator refreshTokenGenerator;
    private final long refreshTokenExpirationSeconds;

    public AuthService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            RefreshTokenGenerator refreshTokenGenerator,
            @Value("${app.jwt.refresh-token-expiration-seconds}") long refreshTokenExpirationSeconds
    ) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenGenerator = refreshTokenGenerator;
        this.refreshTokenExpirationSeconds = refreshTokenExpirationSeconds;
    }

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

    @Transactional
    public LoginResult login(LoginRequest request) {
        String email = normalizeEmail(request.email());

        // 계정 상태·비밀번호 불일치를 동일한 오류로 응답해 이메일 존재 여부를 노출하지 않는다.
        User user = userRepository.findByEmail(email)
                .filter(candidate -> candidate.getStatus() == UserStatus.ACTIVE)
                .filter(candidate -> passwordEncoder.matches(request.password(), candidate.getPassword()))
                .orElseThrow(() -> new ApplicationException(AuthErrorCode.AUTHENTICATION_FAILED));

        String accessToken = jwtTokenProvider.issueAccessToken(user.getId());
        String refreshToken = issueRefreshToken(user);
        return new LoginResult(LoginResponse.of(accessToken, user), refreshToken);
    }

    public MeResponse getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(AuthErrorCode.INVALID_TOKEN));
        return MeResponse.from(user);
    }

    @Transactional
    public MeResponse updateProfile(Long userId, MeUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(AuthErrorCode.INVALID_TOKEN));

        String nickname = request.nickname() != null ? request.nickname().trim() : null;
        if (nickname != null && !nickname.equals(user.getNickname()) && userRepository.existsByNickname(nickname)) {
            throw new ApplicationException(AuthErrorCode.DUPLICATE_NICKNAME);
        }

        user.updateProfile(nickname, request.profileUrl(), request.introduction());
        return MeResponse.from(user);
    }

    @Transactional
    public ReissueResult reissue(String rawRefreshToken) {
        RefreshToken current = findActiveRefreshToken(rawRefreshToken);
        current.revoke(LocalDateTime.now());
        refreshTokenRepository.save(current);

        User user = current.getUser();
        String accessToken = jwtTokenProvider.issueAccessToken(user.getId());
        String refreshToken = issueRefreshToken(user);
        return new ReissueResult(accessToken, refreshToken);
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        if (rawRefreshToken == null) {
            return;
        }

        refreshTokenRepository.findByTokenHash(refreshTokenGenerator.hash(rawRefreshToken))
                .ifPresent(refreshToken -> {
                    refreshToken.revoke(LocalDateTime.now());
                    refreshTokenRepository.save(refreshToken);
                });
    }

    private RefreshToken findActiveRefreshToken(String rawRefreshToken) {
        if (rawRefreshToken == null) {
            throw new ApplicationException(AuthErrorCode.AUTHENTICATION_REQUIRED);
        }

        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(refreshTokenGenerator.hash(rawRefreshToken))
                .orElseThrow(() -> new ApplicationException(AuthErrorCode.INVALID_TOKEN));

        if (!refreshToken.isActive(LocalDateTime.now())) {
            throw new ApplicationException(AuthErrorCode.INVALID_TOKEN);
        }
        return refreshToken;
    }

    private String issueRefreshToken(User user) {
        String rawToken = refreshTokenGenerator.generateRawToken();
        String hash = refreshTokenGenerator.hash(rawToken);
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(refreshTokenExpirationSeconds);
        refreshTokenRepository.save(RefreshToken.issue(user, hash, expiresAt));
        return rawToken;
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    public record LoginResult(LoginResponse response, String refreshToken) {
    }

    public record ReissueResult(String accessToken, String refreshToken) {
    }
}
