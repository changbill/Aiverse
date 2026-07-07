package com.example.aiverse.repository;

import java.util.Optional;

import com.example.aiverse.entity.User;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(Long id);

    Optional<User> findByIdForUpdate(Long id);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);
}
