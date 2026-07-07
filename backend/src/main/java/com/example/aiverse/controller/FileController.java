package com.example.aiverse.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.aiverse.common.response.ApiResponse;
import com.example.aiverse.dto.UploadRequest;
import com.example.aiverse.dto.UploadResponse;
import com.example.aiverse.service.FileUploadService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "File")
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileUploadService fileUploadService;

    @Operation(summary = "업로드용 Presigned URL 발급")
    @SecurityRequirement(name = "bearer-jwt")
    @PostMapping("/upload")
    public ApiResponse<UploadResponse> upload(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody UploadRequest request
    ) {
        return ApiResponse.of(fileUploadService.issueUploadUrl(userId, request));
    }
}
