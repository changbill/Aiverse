package com.example.aiverse.repository.jpa;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.aiverse.entity.CreditProduct;
import com.example.aiverse.entity.CreditProductStatus;

public interface CreditProductJpaRepository extends JpaRepository<CreditProduct, Long> {

    List<CreditProduct> findAllByStatusOrderByDisplayOrderAsc(CreditProductStatus status);
}
