package com.example.aiverse.repository.impl;

import org.springframework.stereotype.Repository;

import com.example.aiverse.entity.Download;
import com.example.aiverse.repository.DownloadRepository;
import com.example.aiverse.repository.jpa.DownloadJpaRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class DownloadRepositoryImpl implements DownloadRepository {

    private final DownloadJpaRepository downloadJpaRepository;

    @Override
    public Download save(Download download) {
        return downloadJpaRepository.save(download);
    }
}
