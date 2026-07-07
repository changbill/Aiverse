package com.example.aiverse.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.aiverse.entity.Download;

public interface DownloadJpaRepository extends JpaRepository<Download, Long> {
}
