package com.example.aiverse.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.aiverse.dto.DashboardItemResponse;
import com.example.aiverse.dto.DashboardResponse;
import com.example.aiverse.dto.DashboardSeriesItemResponse;
import com.example.aiverse.dto.DashboardTotalsResponse;
import com.example.aiverse.repository.AssetRepository;
import com.example.aiverse.repository.CreatorDailySales;
import com.example.aiverse.repository.CreatorSalesTotals;
import com.example.aiverse.repository.CreatorSettlementRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private static final int TOP_ITEMS_LIMIT = 5;

    private final AssetRepository assetRepository;
    private final CreatorSettlementRepository creatorSettlementRepository;

    public DashboardResponse getSales(Long creatorId, String periodParam) {
        DashboardPeriod period = DashboardPeriod.parse(periodParam);
        LocalDate today = LocalDate.now();
        LocalDateTime from = period.from(today);

        long assetCount = assetRepository.countByCreatorId(creatorId);
        CreatorSalesTotals totals = creatorSettlementRepository.sumSales(creatorId, from);

        List<DashboardSeriesItemResponse> series = buildSeries(creatorId, period, today);
        List<DashboardItemResponse> items = creatorSettlementRepository
                .findTopAssetSales(creatorId, from, TOP_ITEMS_LIMIT)
                .stream()
                .map(row -> new DashboardItemResponse(row.assetId(), row.title(), row.salesCount(), row.revenueCredit()))
                .toList();

        return new DashboardResponse(
                new DashboardTotalsResponse(assetCount, totals.salesCount(), totals.revenueCredit()),
                series,
                items
        );
    }

    private List<DashboardSeriesItemResponse> buildSeries(Long creatorId, DashboardPeriod period, LocalDate today) {
        LocalDate startDate = period.startDate(today);
        LocalDateTime to = today.atTime(LocalTime.MAX);

        List<CreatorDailySales> dailySales = creatorSettlementRepository.findDailySales(
                creatorId, startDate == null ? null : startDate.atStartOfDay(), to
        );

        if (startDate == null) {
            return dailySales.stream()
                    .map(row -> new DashboardSeriesItemResponse(row.date(), row.salesCount(), row.revenueCredit()))
                    .toList();
        }

        Map<LocalDate, CreatorDailySales> byDate = dailySales.stream()
                .collect(Collectors.toMap(CreatorDailySales::date, Function.identity()));

        List<DashboardSeriesItemResponse> series = new ArrayList<>();
        for (LocalDate date = startDate; !date.isAfter(today); date = date.plusDays(1)) {
            CreatorDailySales row = byDate.get(date);
            series.add(new DashboardSeriesItemResponse(
                    date, row == null ? 0 : row.salesCount(), row == null ? 0 : row.revenueCredit()
            ));
        }
        return series;
    }
}
