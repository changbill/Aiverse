package com.example.aiverse.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.aiverse.dto.CreditProductResponse;
import com.example.aiverse.repository.CreditProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CreditProductService {

    private final CreditProductRepository creditProductRepository;

    public List<CreditProductResponse> getActiveProducts() {
        return creditProductRepository.findAllActiveOrderByDisplayOrder().stream()
                .map(CreditProductResponse::from)
                .toList();
    }
}
