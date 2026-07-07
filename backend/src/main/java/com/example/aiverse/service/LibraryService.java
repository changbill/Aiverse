package com.example.aiverse.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.aiverse.common.response.PageResponse;
import com.example.aiverse.dto.LibraryItemResponse;
import com.example.aiverse.repository.PurchaseRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LibraryService {

    private final PurchaseRepository purchaseRepository;

    public PageResponse<LibraryItemResponse> getLibrary(Long userId, int page, int size) {
        int boundedPage = Math.max(page, 0);
        int boundedSize = Math.min(Math.max(size, 1), 100);
        return PageResponse.from(
                purchaseRepository.searchByUserId(userId, PageRequest.of(boundedPage, boundedSize)),
                LibraryItemResponse::from
        );
    }
}
