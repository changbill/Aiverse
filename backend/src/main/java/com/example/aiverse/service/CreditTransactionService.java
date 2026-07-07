package com.example.aiverse.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.aiverse.common.response.PageResponse;
import com.example.aiverse.dto.CreditTransactionResponse;
import com.example.aiverse.entity.CreditTransactionType;
import com.example.aiverse.repository.CreditTransactionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CreditTransactionService {

    private final CreditTransactionRepository creditTransactionRepository;

    public PageResponse<CreditTransactionResponse> search(Long userId, CreditTransactionType type, int page, int size) {
        int boundedPage = Math.max(page, 0);
        int boundedSize = Math.min(Math.max(size, 1), 100);
        return PageResponse.from(
                creditTransactionRepository.search(userId, type, PageRequest.of(boundedPage, boundedSize)),
                CreditTransactionResponse::from
        );
    }
}
