package com.example.aiverse.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.aiverse.dto.DashboardResponse;
import com.example.aiverse.repository.AssetRepository;
import com.example.aiverse.repository.CreatorAssetSales;
import com.example.aiverse.repository.CreatorDailySales;
import com.example.aiverse.repository.CreatorSalesTotals;
import com.example.aiverse.repository.CreatorSettlementRepository;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private AssetRepository assetRepository;

    @Mock
    private CreatorSettlementRepository creatorSettlementRepository;

    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        dashboardService = new DashboardService(assetRepository, creatorSettlementRepository);
    }

    @Test
    void 총계와_상위_콘텐츠를_그대로_전달한다() {
        given(assetRepository.countByCreatorId(5L)).willReturn(3L);
        given(creatorSettlementRepository.sumSales(eq(5L), any())).willReturn(new CreatorSalesTotals(10, 800));
        given(creatorSettlementRepository.findDailySales(eq(5L), any(), any())).willReturn(List.of());
        given(creatorSettlementRepository.findTopAssetSales(eq(5L), any(), eq(5))).willReturn(List.of(
                new CreatorAssetSales(1L, "베스트셀러", 7, 560)
        ));

        DashboardResponse response = dashboardService.getSales(5L, "30D");

        assertThat(response.totals().assetCount()).isEqualTo(3);
        assertThat(response.totals().totalSales()).isEqualTo(10);
        assertThat(response.totals().totalRevenueCredit()).isEqualTo(800);
        assertThat(response.items()).hasSize(1);
        assertThat(response.items().getFirst().assetId()).isEqualTo(1L);
    }

    @Test
    void 기간_내_판매가_없는_날짜는_0으로_채운_series를_반환한다() {
        LocalDate today = LocalDate.now();
        given(assetRepository.countByCreatorId(5L)).willReturn(0L);
        given(creatorSettlementRepository.sumSales(eq(5L), any())).willReturn(new CreatorSalesTotals(1, 80));
        given(creatorSettlementRepository.findDailySales(eq(5L), any(), any()))
                .willReturn(List.of(new CreatorDailySales(today, 1, 80)));
        given(creatorSettlementRepository.findTopAssetSales(eq(5L), any(), eq(5))).willReturn(List.of());

        DashboardResponse response = dashboardService.getSales(5L, "7D");

        assertThat(response.series()).hasSize(7);
        assertThat(response.series().getLast().date()).isEqualTo(today);
        assertThat(response.series().getLast().salesCount()).isEqualTo(1);
        assertThat(response.series().get(0).salesCount()).isEqualTo(0);
    }

    @Test
    void ALL_기간은_실제_판매가_있는_날짜만_series로_반환한다() {
        LocalDate saleDate = LocalDate.now().minusDays(100);
        given(assetRepository.countByCreatorId(5L)).willReturn(1L);
        given(creatorSettlementRepository.sumSales(eq(5L), isNull())).willReturn(new CreatorSalesTotals(1, 80));
        given(creatorSettlementRepository.findDailySales(eq(5L), isNull(), any()))
                .willReturn(List.of(new CreatorDailySales(saleDate, 1, 80)));
        given(creatorSettlementRepository.findTopAssetSales(eq(5L), isNull(), eq(5))).willReturn(List.of());

        DashboardResponse response = dashboardService.getSales(5L, "ALL");

        assertThat(response.series()).hasSize(1);
        assertThat(response.series().getFirst().date()).isEqualTo(saleDate);
    }
}
