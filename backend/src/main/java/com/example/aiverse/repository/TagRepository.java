package com.example.aiverse.repository;

import java.util.Optional;

import com.example.aiverse.entity.Tag;

public interface TagRepository {

    Tag save(Tag tag);

    Optional<Tag> findByName(String name);

    boolean existsByName(String name);
}
