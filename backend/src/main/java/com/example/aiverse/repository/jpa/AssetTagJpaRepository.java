package com.example.aiverse.repository.jpa;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.aiverse.entity.AssetTag;

public interface AssetTagJpaRepository extends JpaRepository<AssetTag, Long> {

    @Query("""
            SELECT assetTag
            FROM AssetTag assetTag
            JOIN FETCH assetTag.tag
            WHERE assetTag.asset.id = :assetId
            """)
    List<AssetTag> findByAssetId(@Param("assetId") Long assetId);
}
