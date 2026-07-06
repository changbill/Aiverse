package com.example.aiverse.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.aiverse.common.response.ApiResponse;
import com.example.aiverse.dto.TagResponse;
import com.example.aiverse.service.TagService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Tag")
@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @Operation(summary = "태그 검색·사용량순 목록")
    @GetMapping
    public ApiResponse<List<TagResponse>> getTags(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Integer limit
    ) {
        return ApiResponse.of(tagService.search(query, limit));
    }
}
