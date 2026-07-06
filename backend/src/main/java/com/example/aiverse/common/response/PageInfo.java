package com.example.aiverse.common.response;

import org.springframework.data.domain.Page;

public record PageInfo(
        int number,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {

    public static PageInfo from(Page<?> page) {
        return new PageInfo(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext()
        );
    }
}
