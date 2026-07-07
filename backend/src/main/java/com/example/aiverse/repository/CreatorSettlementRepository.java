package com.example.aiverse.repository;

import java.time.LocalDateTime;

import com.example.aiverse.entity.CreatorSettlement;

public interface CreatorSettlementRepository {

    CreatorSettlement save(CreatorSettlement creatorSettlement);

    CreatorSalesTotals sumSales(Long creatorId, LocalDateTime from);
}
