package com.example.aiverse.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import com.example.aiverse.entity.User;
import com.example.aiverse.support.RepositoryIntegrationTestSupport;

class UserRepositoryTest extends RepositoryIntegrationTestSupport {

    @Autowired
    private UserRepository userRepository;

    @Test
    void 저장한_유저를_ID로_조회할_수_있다() {
        User user = User.register("user1@example.com", "encoded-password", "nick1");

        User saved = userRepository.save(user);

        assertThat(saved.getId()).isNotNull();
        assertThat(userRepository.findById(saved.getId()))
                .isPresent()
                .get()
                .extracting(User::getEmail)
                .isEqualTo("user1@example.com");
    }

    @Test
    void 이메일로_유저를_조회할_수_있다() {
        userRepository.save(User.register("user2@example.com", "encoded-password", "nick2"));

        assertThat(userRepository.findByEmail("user2@example.com"))
                .isPresent()
                .get()
                .extracting(User::getNickname)
                .isEqualTo("nick2");
    }

    @Test
    void 존재하지_않는_이메일_조회시_빈값을_반환한다() {
        assertThat(userRepository.findByEmail("nobody@example.com")).isEmpty();
    }

    @Test
    void 이메일_존재_여부를_확인할_수_있다() {
        userRepository.save(User.register("user3@example.com", "encoded-password", "nick3"));

        assertThat(userRepository.existsByEmail("user3@example.com")).isTrue();
        assertThat(userRepository.existsByEmail("nobody@example.com")).isFalse();
    }

    @Test
    void 닉네임_존재_여부를_확인할_수_있다() {
        userRepository.save(User.register("user4@example.com", "encoded-password", "nick4"));

        assertThat(userRepository.existsByNickname("nick4")).isTrue();
        assertThat(userRepository.existsByNickname("nobody")).isFalse();
    }

    @Test
    void 이메일_중복_저장시_유니크_제약_위반_예외가_발생한다() {
        userRepository.save(User.register("dup@example.com", "encoded-password", "nick-a"));

        assertThatThrownBy(() -> userRepository.save(User.register("dup@example.com", "encoded-password", "nick-b")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void 닉네임_중복_저장시_유니크_제약_위반_예외가_발생한다() {
        userRepository.save(User.register("nick-dup-a@example.com", "encoded-password", "dupnick"));

        assertThatThrownBy(
                () -> userRepository.save(User.register("nick-dup-b@example.com", "encoded-password", "dupnick")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
