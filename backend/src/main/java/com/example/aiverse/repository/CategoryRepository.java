package com.example.aiverse.repository;

import java.util.List;
import java.util.Optional;

import com.example.aiverse.entity.Category;

public interface CategoryRepository {

    Optional<Category> findById(Long id);

    List<Category> findAllActiveOrderByDisplayOrder();
}
