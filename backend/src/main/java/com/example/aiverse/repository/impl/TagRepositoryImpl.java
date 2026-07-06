package com.example.aiverse.repository.impl;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.example.aiverse.entity.Tag;
import com.example.aiverse.repository.TagRepository;
import com.example.aiverse.repository.jpa.TagJpaRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class TagRepositoryImpl implements TagRepository {

    private final TagJpaRepository tagJpaRepository;

    @Override
    public Tag save(Tag tag) {
        return tagJpaRepository.save(tag);
    }

    @Override
    public Optional<Tag> findByName(String name) {
        return tagJpaRepository.findByName(name);
    }

    @Override
    public boolean existsByName(String name) {
        return tagJpaRepository.existsByName(name);
    }
}
