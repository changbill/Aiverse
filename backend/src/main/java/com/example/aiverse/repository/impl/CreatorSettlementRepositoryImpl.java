package com.example.aiverse.repository.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.example.aiverse.entity.CreatorSettlement;
import com.example.aiverse.repository.CreatorAssetSales;
import com.example.aiverse.repository.CreatorDailySales;
import com.example.aiverse.repository.CreatorSalesTotals;
import com.example.aiverse.repository.CreatorSettlementRepository;
import com.example.aiverse.repository.jpa.CreatorSettlementJpaRepository;
import com.example.aiverse.repository.querydsl.CreatorSettlementQuerydslRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CreatorSettlementRepositoryImpl implements CreatorSettlementRepository {

    private final CreatorSettlementJpaRepository creatorSettlementJpaRepository;
    private final CreatorSettlementQuerydslRepository creatorSettlementQuerydslRepository;

    @Override
    public CreatorSettlement save(CreatorSettlement creatorSettlement) {
        return creatorSettlementJpaRepository.save(creatorSettlement);
    }

    @Override
    public CreatorSalesTotals sumSales(Long creatorId, LocalDateTime from) {
        return creatorSettlementQuerydslRepository.sumSales(creatorId, from);
    }

    @Override
    public List<CreatorDailySales> findDailySales(Long creatorId, LocalDateTime from, LocalDateTime to) {
        return creatorSettlementQuerydslRepository.findDailySales(creatorId, from, to);
    }

    @Override
    public List<CreatorAssetSales> findTopAssetSales(Long creatorId, LocalDateTime from, int limit) {
        return creatorSettlementQuerydslRepository.findTopAssetSales(creatorId, from, limit);
    }
}
