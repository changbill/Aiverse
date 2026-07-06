package com.example.aiverse.repository.impl;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.example.aiverse.entity.Asset;
import com.example.aiverse.entity.AssetStatus;
import com.example.aiverse.entity.QAsset;
import com.example.aiverse.entity.QAssetTag;
import com.example.aiverse.entity.QPurchase;
import com.example.aiverse.entity.QTag;
import com.example.aiverse.repository.AssetRepository;
import com.example.aiverse.repository.AssetSearchCondition;
import com.example.aiverse.repository.AssetSort;
import com.example.aiverse.repository.jpa.AssetJpaRepository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class AssetRepositoryImpl implements AssetRepository {

    private final AssetJpaRepository assetJpaRepository;
    private final EntityManager entityManager;

    @Override
    public Asset save(Asset asset) {
        return assetJpaRepository.save(asset);
    }

    @Override
    public Optional<Asset> findById(Long id) {
        return assetJpaRepository.findById(id);
    }

    @Override
    public Page<Asset> search(AssetSearchCondition condition, Pageable pageable) {
        QAsset asset = QAsset.asset;
        QAssetTag assetTag = QAssetTag.assetTag;
        QTag tag = QTag.tag;
        QPurchase purchase = QPurchase.purchase;
        BooleanBuilder where = conditions(condition, asset, tag);
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);

        JPAQuery<Asset> contentQuery = queryFactory.selectFrom(asset)
                .leftJoin(assetTag).on(assetTag.asset.eq(asset))
                .leftJoin(assetTag.tag, tag)
                .leftJoin(purchase).on(purchase.asset.eq(asset))
                .join(asset.creator).fetchJoin()
                .join(asset.category).fetchJoin()
                .where(where)
                .groupBy(asset.id)
                .orderBy(order(condition.effectiveSort(), asset, purchase))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        Long total = queryFactory.select(asset.id.countDistinct())
                .from(asset)
                .leftJoin(assetTag).on(assetTag.asset.eq(asset))
                .leftJoin(assetTag.tag, tag)
                .where(where)
                .fetchOne();

        return new PageImpl<>(contentQuery.fetch(), pageable, total == null ? 0 : total);
    }

    private BooleanBuilder conditions(AssetSearchCondition condition, QAsset asset, QTag tag) {
        BooleanBuilder where = new BooleanBuilder(asset.status.eq(AssetStatus.PUBLISHED));
        if (condition.search() != null && !condition.search().isBlank()) {
            String search = condition.search().trim();
            where.and(asset.title.containsIgnoreCase(search)
                    .or(asset.description.containsIgnoreCase(search))
                    .or(tag.name.containsIgnoreCase(search)));
        }
        if (condition.type() != null) where.and(asset.assetType.eq(condition.type()));
        if (condition.categoryId() != null) where.and(asset.category.id.eq(condition.categoryId()));
        if (condition.tag() != null && !condition.tag().isBlank()) where.and(tag.name.eq(condition.tag().trim().toLowerCase()));
        if (condition.minPrice() != null) where.and(asset.priceCredit.goe(condition.minPrice()));
        if (condition.maxPrice() != null) where.and(asset.priceCredit.loe(condition.maxPrice()));
        if (condition.creatorId() != null) where.and(asset.creator.id.eq(condition.creatorId()));
        return where;
    }

    private OrderSpecifier<?>[] order(AssetSort sort, QAsset asset, QPurchase purchase) {
        return switch (sort) {
            case POPULAR -> new OrderSpecifier<?>[]{purchase.id.count().desc(), asset.createdAt.desc(), asset.id.desc()};
            case PRICE_ASC -> new OrderSpecifier<?>[]{asset.priceCredit.asc(), asset.createdAt.desc(), asset.id.desc()};
            case PRICE_DESC -> new OrderSpecifier<?>[]{asset.priceCredit.desc(), asset.createdAt.desc(), asset.id.desc()};
            case LATEST -> new OrderSpecifier<?>[]{asset.createdAt.desc(), asset.id.desc()};
        };
    }
}
