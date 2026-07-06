package com.example.aiverse.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.aiverse.common.error.ApplicationException;
import com.example.aiverse.common.error.AuthErrorCode;
import com.example.aiverse.dto.LoginRequest;
import com.example.aiverse.dto.MeResponse;
import com.example.aiverse.dto.RegisterRequest;
import com.example.aiverse.dto.RegisterResponse;
import com.example.aiverse.entity.RefreshToken;
import com.example.aiverse.entity.User;
import com.example.aiverse.entity.UserRole;
import com.example.aiverse.entity.UserStatus;
import com.example.aiverse.repository.RefreshTokenRepository;
import com.example.aiverse.repository.UserRepository;
import com.example.aiverse.security.JwtTokenProvider;
import com.example.aiverse.security.RefreshTokenGenerator;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private RefreshTokenGenerator refreshTokenGenerator;

    private AuthService authService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        authService = new AuthService(
                userRepository, refreshTokenRepository, passwordEncoder, jwtTokenProvider, refreshTokenGenerator,
                1_209_600L
        );
    }

    @Test
    void 회원가입에_성공한다() {
        RegisterRequest request = new RegisterRequest("User@Example.com", "password1234", "닉네임");
        given(userRepository.existsByEmail("user@example.com")).willReturn(false);
        given(userRepository.existsByNickname("닉네임")).willReturn(false);
        given(passwordEncoder.encode("password1234")).willReturn("encoded-password");
        User saved = withId(User.register("user@example.com", "encoded-password", "닉네임"), 1L);
        given(userRepository.save(any(User.class))).willReturn(saved);

        RegisterResponse response = authService.register(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("user@example.com");
        assertThat(response.nickname()).isEqualTo("닉네임");
        assertThat(response.role()).isEqualTo(UserRole.USER);
        assertThat(response.creditBalance()).isEqualTo(0);
    }

    @Test
    void 이메일이_중복되면_예외가_발생한다() {
        RegisterRequest request = new RegisterRequest("dup@example.com", "password1234", "닉네임");
        given(userRepository.existsByEmail("dup@example.com")).willReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.DUPLICATE_EMAIL);

        verify(userRepository, never()).save(any());
    }

    @Test
    void 닉네임이_중복되면_예외가_발생한다() {
        RegisterRequest request = new RegisterRequest("user@example.com", "password1234", "중복닉네임");
        given(userRepository.existsByEmail("user@example.com")).willReturn(false);
        given(userRepository.existsByNickname("중복닉네임")).willReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.DUPLICATE_NICKNAME);

        verify(userRepository, never()).save(any());
    }

    @Test
    void 로그인에_성공하면_access_토큰과_refresh_토큰을_발급한다() {
        User user = withId(User.register("user@example.com", "encoded-password", "닉네임"), 1L);
        given(userRepository.findByEmail("user@example.com")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("password1234", "encoded-password")).willReturn(true);
        given(jwtTokenProvider.issueAccessToken(1L)).willReturn("access-token");
        given(refreshTokenGenerator.generateRawToken()).willReturn("raw-refresh-token");
        given(refreshTokenGenerator.hash("raw-refresh-token")).willReturn("hashed-refresh-token");

        AuthService.LoginResult result = authService.login(new LoginRequest("user@example.com", "password1234"));

        assertThat(result.response().accessToken()).isEqualTo("access-token");
        assertThat(result.response().user().id()).isEqualTo(1L);
        assertThat(result.refreshToken()).isEqualTo("raw-refresh-token");
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void 존재하지_않는_이메일로_로그인하면_인증_실패_예외가_발생한다() {
        given(userRepository.findByEmail("nobody@example.com")).willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("nobody@example.com", "password1234")))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.AUTHENTICATION_FAILED);
    }

    @Test
    void 비밀번호가_일치하지_않으면_인증_실패_예외가_발생한다() {
        User user = withId(User.register("user@example.com", "encoded-password", "닉네임"), 1L);
        given(userRepository.findByEmail("user@example.com")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("wrong-password", "encoded-password")).willReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("user@example.com", "wrong-password")))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.AUTHENTICATION_FAILED);
    }

    @Test
    void 비활성_계정은_로그인할_수_없다() {
        User user = withStatus(
                withId(User.register("user@example.com", "encoded-password", "닉네임"), 1L),
                UserStatus.DELETED
        );
        given(userRepository.findByEmail("user@example.com")).willReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(new LoginRequest("user@example.com", "password1234")))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.AUTHENTICATION_FAILED);
    }

    @Test
    void 현재_사용자_정보를_조회한다() {
        User user = withId(User.register("user@example.com", "encoded-password", "닉네임"), 1L);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        MeResponse response = authService.getCurrentUser(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("user@example.com");
        assertThat(response.status()).isEqualTo(UserStatus.ACTIVE);
        assertThat(response.creditBalance()).isEqualTo(0);
    }

    @Test
    void 토큰은_유효하지만_사용자가_존재하지_않으면_예외가_발생한다() {
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.getCurrentUser(999L))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.INVALID_TOKEN);
    }

    @Test
    void 유효한_refresh_토큰으로_재발급하면_기존_토큰은_폐기되고_새_토큰이_발급된다() {
        User user = withId(User.register("user@example.com", "encoded-password", "닉네임"), 1L);
        RefreshToken stored = RefreshToken.issue(user, "old-hash", LocalDateTime.now().plusDays(1));
        given(refreshTokenGenerator.hash("old-raw-token")).willReturn("old-hash");
        given(refreshTokenRepository.findByTokenHash("old-hash")).willReturn(Optional.of(stored));
        given(jwtTokenProvider.issueAccessToken(1L)).willReturn("new-access-token");
        given(refreshTokenGenerator.generateRawToken()).willReturn("new-raw-token");
        given(refreshTokenGenerator.hash("new-raw-token")).willReturn("new-hash");

        AuthService.ReissueResult result = authService.reissue("old-raw-token");

        assertThat(result.accessToken()).isEqualTo("new-access-token");
        assertThat(result.refreshToken()).isEqualTo("new-raw-token");
        assertThat(stored.isActive(LocalDateTime.now())).isFalse();
        verify(refreshTokenRepository).save(stored);
    }

    @Test
    void refresh_토큰_쿠키가_없으면_인증이_필요하다는_예외가_발생한다() {
        assertThatThrownBy(() -> authService.reissue(null))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.AUTHENTICATION_REQUIRED);
    }

    @Test
    void 존재하지_않는_refresh_토큰으로_재발급하면_예외가_발생한다() {
        given(refreshTokenGenerator.hash("unknown-token")).willReturn("unknown-hash");
        given(refreshTokenRepository.findByTokenHash("unknown-hash")).willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.reissue("unknown-token"))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.INVALID_TOKEN);
    }

    @Test
    void 만료된_refresh_토큰으로_재발급하면_예외가_발생한다() {
        User user = withId(User.register("user@example.com", "encoded-password", "닉네임"), 1L);
        RefreshToken expired = RefreshToken.issue(user, "expired-hash", LocalDateTime.now().minusSeconds(1));
        given(refreshTokenGenerator.hash("expired-token")).willReturn("expired-hash");
        given(refreshTokenRepository.findByTokenHash("expired-hash")).willReturn(Optional.of(expired));

        assertThatThrownBy(() -> authService.reissue("expired-token"))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.INVALID_TOKEN);
    }

    @Test
    void 이미_폐기된_refresh_토큰으로_재발급하면_예외가_발생한다() {
        User user = withId(User.register("user@example.com", "encoded-password", "닉네임"), 1L);
        RefreshToken revoked = RefreshToken.issue(user, "revoked-hash", LocalDateTime.now().plusDays(1));
        revoked.revoke(LocalDateTime.now());
        given(refreshTokenGenerator.hash("revoked-token")).willReturn("revoked-hash");
        given(refreshTokenRepository.findByTokenHash("revoked-hash")).willReturn(Optional.of(revoked));

        assertThatThrownBy(() -> authService.reissue("revoked-token"))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.INVALID_TOKEN);
    }

    @Test
    void 유효한_refresh_토큰으로_로그아웃하면_해당_세션만_폐기한다() {
        User user = withId(User.register("user@example.com", "encoded-password", "닉네임"), 1L);
        RefreshToken stored = RefreshToken.issue(user, "hash", LocalDateTime.now().plusDays(1));
        given(refreshTokenGenerator.hash("raw-token")).willReturn("hash");
        given(refreshTokenRepository.findByTokenHash("hash")).willReturn(Optional.of(stored));

        authService.logout("raw-token");

        assertThat(stored.isActive(LocalDateTime.now())).isFalse();
        verify(refreshTokenRepository).save(stored);
    }

    @Test
    void 존재하지_않는_refresh_토큰으로_로그아웃해도_예외가_발생하지_않는다() {
        given(refreshTokenGenerator.hash("unknown-token")).willReturn("unknown-hash");
        given(refreshTokenRepository.findByTokenHash("unknown-hash")).willReturn(Optional.empty());

        assertThatCode(() -> authService.logout("unknown-token")).doesNotThrowAnyException();

        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void refresh_토큰_쿠키_없이_로그아웃해도_예외가_발생하지_않는다() {
        assertThatCode(() -> authService.logout(null)).doesNotThrowAnyException();

        verify(refreshTokenRepository, never()).findByTokenHash(any());
    }

    private User withId(User user, Long id) {
        setField(user, "id", id);
        return user;
    }

    private User withStatus(User user, UserStatus status) {
        setField(user, "status", status);
        return user;
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            Field field = User.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
