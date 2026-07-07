package com.example.aiverse.repository.jpa;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.aiverse.entity.Asset;
import com.example.aiverse.entity.AssetStatus;

public interface AssetJpaRepository extends JpaRepository<Asset, Long> {

    @Query("""
            SELECT asset
            FROM Asset asset
            JOIN FETCH asset.creator
            JOIN FETCH asset.category
            WHERE asset.id = :id
            AND asset.status = :status
            """)
    Optional<Asset> findByIdAndStatus(@Param("id") Long id, @Param("status") AssetStatus status);

    // creator를 fetch join하지 않는다 — 구매 흐름에서 findByIdForUpdate로 잠그기 전에
    // creator User가 세션 캐시에 잠기지 않은 채로 올라가면, 이후 잠금 조회가 이미 캐시된
    // (갱신되지 않은) 인스턴스를 그대로 반환해 잠금 후 최신 값을 읽지 못하는 문제가 생긴다.
    @Query("""
            SELECT asset
            FROM Asset asset
            WHERE asset.id = :id
            AND asset.status = :status
            """)
    Optional<Asset> findPurchasableById(@Param("id") Long id, @Param("status") AssetStatus status);

    @Query("""
            SELECT CASE WHEN COUNT(asset) > 0 THEN TRUE ELSE FALSE END
            FROM Asset asset
            WHERE asset.previewObjectKey = :objectKey
            OR asset.originalObjectKey = :objectKey
            """)
    boolean existsByObjectKey(@Param("objectKey") String objectKey);

    long countByCreatorId(Long creatorId);
}
