package com.example.aiverse.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.aiverse.entity.Asset;

public interface AssetJpaRepository extends JpaRepository<Asset, Long> {
}
