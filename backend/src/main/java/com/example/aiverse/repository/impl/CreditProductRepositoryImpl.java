package com.example.aiverse.repository.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.example.aiverse.entity.CreditProduct;
import com.example.aiverse.entity.CreditProductStatus;
import com.example.aiverse.repository.CreditProductRepository;
import com.example.aiverse.repository.jpa.CreditProductJpaRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CreditProductRepositoryImpl implements CreditProductRepository {

    private final CreditProductJpaRepository creditProductJpaRepository;

    @Override
    public Optional<CreditProduct> findById(Long id) {
        return creditProductJpaRepository.findById(id);
    }

    @Override
    public List<CreditProduct> findAllActiveOrderByDisplayOrder() {
        return creditProductJpaRepository.findAllByStatusOrderByDisplayOrderAsc(CreditProductStatus.ACTIVE);
    }
}
