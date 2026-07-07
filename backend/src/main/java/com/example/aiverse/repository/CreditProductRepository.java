package com.example.aiverse.repository;

import java.util.List;
import java.util.Optional;

import com.example.aiverse.entity.CreditProduct;

public interface CreditProductRepository {

    Optional<CreditProduct> findById(Long id);

    List<CreditProduct> findAllActiveOrderByDisplayOrder();
}
