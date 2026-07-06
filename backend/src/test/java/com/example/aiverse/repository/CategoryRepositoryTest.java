package com.example.aiverse.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.example.aiverse.entity.CategoryName;
import com.example.aiverse.support.IntegrationTestSupport;

class CategoryRepositoryTest extends IntegrationTestSupport {

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void ID로_카테고리를_조회할_수_있다() {
        assertThat(categoryRepository.findById(1L))
                .isPresent()
                .get()
                .extracting(category -> category.getName())
                .isEqualTo(CategoryName.NATURE);
    }

    @Test
    void 활성_카테고리를_표시_순서대로_조회한다() {
        var categories = categoryRepository.findAllActiveOrderByDisplayOrder();

        assertThat(categories).hasSize(8);
        assertThat(categories.get(0).getName()).isEqualTo(CategoryName.NATURE);
        assertThat(categories.get(7).getName()).isEqualTo(CategoryName.OTHER);
        assertThat(categories).allMatch(category -> category.isActive());
    }
}
