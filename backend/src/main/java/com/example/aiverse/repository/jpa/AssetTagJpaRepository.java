package com.example.aiverse.repository.jpa;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.aiverse.entity.AssetTag;

public interface AssetTagJpaRepository extends JpaRepository<AssetTag, Long> {

    // tag를 즉시 로딩해 목록 조회 시 태그 이름을 읽을 때 N+1과 지연 로딩 예외를 피한다.
    @EntityGraph(attributePaths = "tag")
    List<AssetTag> findByAssetId(Long assetId);
}
