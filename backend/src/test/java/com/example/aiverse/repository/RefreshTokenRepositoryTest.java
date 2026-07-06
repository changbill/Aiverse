package com.example.aiverse.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.example.aiverse.entity.RefreshToken;
import com.example.aiverse.entity.User;
import com.example.aiverse.support.RepositoryIntegrationTestSupport;

class RefreshTokenRepositoryTest extends RepositoryIntegrationTestSupport {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    private User persistUser(String email, String nickname) {
        return userRepository.save(User.register(email, "encoded-password", nickname));
    }

    @Test
    void 저장한_리프레시_토큰을_토큰_해시로_조회할_수_있다() {
        User user = persistUser("rt1@example.com", "rt-nick1");
        RefreshToken token = RefreshToken.issue(user, "hash-1", LocalDateTime.now().plusDays(14));

        refreshTokenRepository.save(token);

        assertThat(refreshTokenRepository.findByTokenHash("hash-1"))
                .isPresent()
                .get()
                .extracting(RefreshToken::getTokenHash)
                .isEqualTo("hash-1");
    }

    @Test
    void 존재하지_않는_토큰_해시_조회시_빈값을_반환한다() {
        assertThat(refreshTokenRepository.findByTokenHash("no-such-hash")).isEmpty();
    }

    @Test
    void 유효한_세션만_조회하고_폐기되거나_만료된_세션은_제외한다() {
        User user = persistUser("rt2@example.com", "rt-nick2");
        LocalDateTime now = LocalDateTime.now();

        RefreshToken active = RefreshToken.issue(user, "hash-active", now.plusDays(14));
        RefreshToken revoked = RefreshToken.issue(user, "hash-revoked", now.plusDays(14));
        revoked.revoke(now);
        RefreshToken expired = RefreshToken.issue(user, "hash-expired", now.minusSeconds(1));

        refreshTokenRepository.save(active);
        refreshTokenRepository.save(revoked);
        refreshTokenRepository.save(expired);

        List<RefreshToken> result = refreshTokenRepository.findActiveByUserId(user.getId(), now);

        assertThat(result)
                .extracting(RefreshToken::getTokenHash)
                .containsExactly("hash-active");
    }

    @Test
    void 다른_유저의_세션은_활성_세션_조회에_포함되지_않는다() {
        User user1 = persistUser("rt3@example.com", "rt-nick3");
        User user2 = persistUser("rt4@example.com", "rt-nick4");
        LocalDateTime now = LocalDateTime.now();

        refreshTokenRepository.save(RefreshToken.issue(user1, "hash-user1", now.plusDays(14)));
        refreshTokenRepository.save(RefreshToken.issue(user2, "hash-user2", now.plusDays(14)));

        List<RefreshToken> result = refreshTokenRepository.findActiveByUserId(user1.getId(), now);

        assertThat(result)
                .extracting(RefreshToken::getTokenHash)
                .containsExactly("hash-user1");
    }
}
