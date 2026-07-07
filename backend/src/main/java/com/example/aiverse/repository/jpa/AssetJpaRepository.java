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

    @Query("""
            SELECT CASE WHEN COUNT(asset) > 0 THEN TRUE ELSE FALSE END
            FROM Asset asset
            WHERE asset.previewObjectKey = :objectKey
            OR asset.originalObjectKey = :objectKey
            """)
    boolean existsByObjectKey(@Param("objectKey") String objectKey);
}
