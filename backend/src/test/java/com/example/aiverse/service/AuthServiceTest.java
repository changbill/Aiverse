package com.example.aiverse.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Field;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.aiverse.common.error.ApplicationException;
import com.example.aiverse.common.error.AuthErrorCode;
import com.example.aiverse.dto.LoginRequest;
import com.example.aiverse.dto.LoginResponse;
import com.example.aiverse.dto.MeResponse;
import com.example.aiverse.dto.RegisterRequest;
import com.example.aiverse.dto.RegisterResponse;
import com.example.aiverse.entity.User;
import com.example.aiverse.entity.UserRole;
import com.example.aiverse.entity.UserStatus;
import com.example.aiverse.repository.UserRepository;
import com.example.aiverse.security.JwtTokenProvider;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private AuthService authService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, jwtTokenProvider);
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
    void 로그인에_성공하면_토큰을_발급한다() {
        User user = withId(User.register("user@example.com", "encoded-password", "닉네임"), 1L);
        given(userRepository.findByEmail("user@example.com")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("password1234", "encoded-password")).willReturn(true);
        given(jwtTokenProvider.issueAccessToken(1L)).willReturn("access-token");

        LoginResponse response = authService.login(new LoginRequest("user@example.com", "password1234"));

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.user().id()).isEqualTo(1L);
        assertThat(response.user().email()).isEqualTo("user@example.com");
        assertThat(response.user().nickname()).isEqualTo("닉네임");
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
