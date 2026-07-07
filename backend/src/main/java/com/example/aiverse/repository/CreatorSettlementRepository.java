package com.example.aiverse.repository;

import java.time.LocalDateTime;
import java.util.List;

import com.example.aiverse.entity.CreatorSettlement;

public interface CreatorSettlementRepository {

    CreatorSettlement save(CreatorSettlement creatorSettlement);

    CreatorSalesTotals sumSales(Long creatorId, LocalDateTime from);

    List<CreatorDailySales> findDailySales(Long creatorId, LocalDateTime from, LocalDateTime to);

    List<CreatorAssetSales> findTopAssetSales(Long creatorId, LocalDateTime from, int limit);
}
