package com.example.aiverse.repository.jpa;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.aiverse.entity.Category;

public interface CategoryJpaRepository extends JpaRepository<Category, Long> {

    List<Category> findAllByActiveTrueOrderByDisplayOrderAsc();
}
