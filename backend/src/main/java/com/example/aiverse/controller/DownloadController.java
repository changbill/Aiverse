package com.example.aiverse.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.aiverse.common.response.ApiResponse;
import com.example.aiverse.dto.DownloadRequest;
import com.example.aiverse.dto.DownloadResponse;
import com.example.aiverse.service.DownloadService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Download")
@RestController
@RequestMapping("/api/downloads")
@RequiredArgsConstructor
public class DownloadController {

    private final DownloadService downloadService;

    @Operation(summary = "구매 확인 후 원본 다운로드 URL 발급")
    @SecurityRequirement(name = "bearer-jwt")
    @PostMapping
    public ApiResponse<DownloadResponse> download(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody DownloadRequest request
    ) {
        return ApiResponse.of(downloadService.download(userId, request));
    }
}
