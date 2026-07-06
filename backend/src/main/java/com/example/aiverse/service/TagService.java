package com.example.aiverse.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.aiverse.dto.TagResponse;
import com.example.aiverse.repository.TagRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagService {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 50;
    private static final int MIN_LIMIT = 1;

    private final TagRepository tagRepository;

    public List<TagResponse> search(String query, Integer limit) {
        int effectiveLimit = clampLimit(limit);
        return tagRepository.searchOrderByUsage(query, effectiveLimit).stream()
                .map(TagResponse::from)
                .toList();
    }

    private int clampLimit(Integer limit) {
        if (limit == null) {
            return DEFAULT_LIMIT;
        }
        return Math.max(MIN_LIMIT, Math.min(limit, MAX_LIMIT));
    }
}
