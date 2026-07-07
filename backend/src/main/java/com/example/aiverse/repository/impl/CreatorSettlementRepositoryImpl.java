package com.example.aiverse.repository.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DateTemplate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.example.aiverse.entity.CreatorSettlement;
import com.example.aiverse.entity.QAsset;
import com.example.aiverse.entity.QCreatorSettlement;
import com.example.aiverse.repository.CreatorAssetSales;
import com.example.aiverse.repository.CreatorDailySales;
import com.example.aiverse.repository.CreatorSalesTotals;
import com.example.aiverse.repository.CreatorSettlementRepository;
import com.example.aiverse.repository.jpa.CreatorSettlementJpaRepository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CreatorSettlementRepositoryImpl implements CreatorSettlementRepository {

    private final CreatorSettlementJpaRepository creatorSettlementJpaRepository;
    private final EntityManager entityManager;

    @Override
    public CreatorSettlement save(CreatorSettlement creatorSettlement) {
        return creatorSettlementJpaRepository.save(creatorSettlement);
    }

    @Override
    public CreatorSalesTotals sumSales(Long creatorId, LocalDateTime from) {
        QCreatorSettlement settlement = QCreatorSettlement.creatorSettlement;
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);

        BooleanExpression where = settlement.creator.id.eq(creatorId);
        if (from != null) {
            where = where.and(settlement.settledAt.goe(from));
        }

        NumberExpression<Long> salesCountExpr = settlement.count();
        NumberExpression<Integer> revenueExpr = settlement.settlementCredit.sum();

        Tuple result = queryFactory
                .select(salesCountExpr, revenueExpr)
                .from(settlement)
                .where(where)
                .fetchOne();

        if (result == null) {
            return new CreatorSalesTotals(0, 0);
        }
        long salesCount = Optional.ofNullable(result.get(salesCountExpr)).orElse(0L);
        long revenue = Optional.ofNullable(result.get(revenueExpr)).orElse(0).longValue();
        return new CreatorSalesTotals(salesCount, revenue);
    }

    @Override
    public List<CreatorDailySales> findDailySales(Long creatorId, LocalDateTime from, LocalDateTime to) {
        QCreatorSettlement settlement = QCreatorSettlement.creatorSettlement;
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);

        BooleanExpression where = settlement.creator.id.eq(creatorId);
        if (from != null) {
            where = where.and(settlement.settledAt.goe(from));
        }
        if (to != null) {
            where = where.and(settlement.settledAt.loe(to));
        }

        // JDBC 드라이버가 DATE(...)를 java.sql.Date로 반환하므로 LocalDate로 바로 캐스팅하지 않는다.
        DateTemplate<java.sql.Date> settledDate = Expressions.dateTemplate(java.sql.Date.class, "DATE({0})", settlement.settledAt);
        NumberExpression<Long> salesCountExpr = settlement.count();
        NumberExpression<Integer> revenueExpr = settlement.settlementCredit.sum();

        return queryFactory
                .select(settledDate, salesCountExpr, revenueExpr)
                .from(settlement)
                .where(where)
                .groupBy(settledDate)
                .orderBy(settledDate.asc())
                .fetch()
                .stream()
                .map(tuple -> new CreatorDailySales(
                        tuple.get(settledDate).toLocalDate(),
                        Optional.ofNullable(tuple.get(salesCountExpr)).orElse(0L),
                        Optional.ofNullable(tuple.get(revenueExpr)).orElse(0).longValue()
                ))
                .toList();
    }

    @Override
    public List<CreatorAssetSales> findTopAssetSales(Long creatorId, LocalDateTime from, int limit) {
        QCreatorSettlement settlement = QCreatorSettlement.creatorSettlement;
        QAsset asset = QAsset.asset;
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);

        BooleanExpression where = settlement.creator.id.eq(creatorId);
        if (from != null) {
            where = where.and(settlement.settledAt.goe(from));
        }

        NumberExpression<Long> salesCountExpr = settlement.count();
        NumberExpression<Integer> revenueExpr = settlement.settlementCredit.sum();

        return queryFactory
                .select(asset.id, asset.title, salesCountExpr, revenueExpr)
                .from(settlement)
                .join(settlement.asset, asset)
                .where(where)
                .groupBy(asset.id)
                .orderBy(salesCountExpr.desc(), asset.createdAt.desc())
                .limit(limit)
                .fetch()
                .stream()
                .map(tuple -> new CreatorAssetSales(
                        tuple.get(asset.id),
                        tuple.get(asset.title),
                        Optional.ofNullable(tuple.get(salesCountExpr)).orElse(0L),
                        Optional.ofNullable(tuple.get(revenueExpr)).orElse(0).longValue()
                ))
                .toList();
    }
}
