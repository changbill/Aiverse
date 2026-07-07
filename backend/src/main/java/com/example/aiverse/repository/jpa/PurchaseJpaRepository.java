package com.example.aiverse.repository.jpa;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.aiverse.entity.Purchase;

public interface PurchaseJpaRepository extends JpaRepository<Purchase, Long> {

    boolean existsByAssetId(Long assetId);

    boolean existsByUserIdAndAssetId(Long userId, Long assetId);

    @Query("""
            SELECT p FROM Purchase p
            JOIN FETCH p.asset
            WHERE p.user.id = :userId
            AND p.idempotencyKey = :idempotencyKey
            """)
    Optional<Purchase> findByUserIdAndIdempotencyKey(
            @Param("userId") Long userId,
            @Param("idempotencyKey") String idempotencyKey
    );

    @Query("""
            SELECT p FROM Purchase p
            JOIN FETCH p.asset
            WHERE p.user.id = :userId
            AND p.asset.id = :assetId
            """)
    Optional<Purchase> findByUserIdAndAssetId(@Param("userId") Long userId, @Param("assetId") Long assetId);

    @Query("""
            SELECT p FROM Purchase p
            JOIN FETCH p.asset
            WHERE p.user.id = :userId
            ORDER BY p.purchasedAt DESC, p.id DESC
            """)
    Page<Purchase> searchByUserId(@Param("userId") Long userId, Pageable pageable);
}
