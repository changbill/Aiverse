package com.example.aiverse.repository.impl;

import org.springframework.stereotype.Repository;

import com.example.aiverse.entity.CreatorSettlement;
import com.example.aiverse.repository.CreatorSettlementRepository;
import com.example.aiverse.repository.jpa.CreatorSettlementJpaRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CreatorSettlementRepositoryImpl implements CreatorSettlementRepository {

    private final CreatorSettlementJpaRepository creatorSettlementJpaRepository;

    @Override
    public CreatorSettlement save(CreatorSettlement creatorSettlement) {
        return creatorSettlementJpaRepository.save(creatorSettlement);
    }
}
