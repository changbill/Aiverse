package com.example.aiverse.dto;

import com.example.aiverse.repository.TagUsage;

public record TagResponse(Long id, String name, long usageCount) {

    public static TagResponse from(TagUsage tagUsage) {
        return new TagResponse(tagUsage.id(), tagUsage.name(), tagUsage.usageCount());
    }
}
