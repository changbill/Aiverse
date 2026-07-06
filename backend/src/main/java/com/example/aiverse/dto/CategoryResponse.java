package com.example.aiverse.dto;

import com.example.aiverse.entity.Category;
import com.example.aiverse.entity.CategoryName;

public record CategoryResponse(Long id, CategoryName name, String slug, int displayOrder) {

    public static CategoryResponse from(Category category) {
        return new CategoryResponse(
                category.getId(), category.getName(), category.getSlug(), category.getDisplayOrder()
        );
    }
}
