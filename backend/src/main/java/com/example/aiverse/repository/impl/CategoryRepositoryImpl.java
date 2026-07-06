package com.example.aiverse.repository.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.example.aiverse.entity.Category;
import com.example.aiverse.repository.CategoryRepository;
import com.example.aiverse.repository.jpa.CategoryJpaRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CategoryRepositoryImpl implements CategoryRepository {

    private final CategoryJpaRepository categoryJpaRepository;

    @Override
    public Optional<Category> findById(Long id) {
        return categoryJpaRepository.findById(id);
    }

    @Override
    public List<Category> findAllActiveOrderByDisplayOrder() {
        return categoryJpaRepository.findAllByActiveTrueOrderByDisplayOrderAsc();
    }
}
