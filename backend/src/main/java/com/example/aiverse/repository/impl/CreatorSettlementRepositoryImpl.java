package com.example.aiverse.repository.impl;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.example.aiverse.entity.CreatorSettlement;
import com.example.aiverse.entity.QCreatorSettlement;
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
}
