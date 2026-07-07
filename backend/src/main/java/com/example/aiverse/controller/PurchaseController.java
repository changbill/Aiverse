package com.example.aiverse.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.aiverse.common.response.ApiResponse;
import com.example.aiverse.dto.PurchaseRequest;
import com.example.aiverse.dto.PurchaseResponse;
import com.example.aiverse.service.PurchaseService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Purchase")
@RestController
@RequestMapping("/api/purchases")
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseService purchaseService;

    @Operation(summary = "콘텐츠 구매")
    @SecurityRequirement(name = "bearer-jwt")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PurchaseResponse> purchase(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "재요청 시 최초 처리 결과를 반환하기 위한 멱등성 키") @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody PurchaseRequest request
    ) {
        return ApiResponse.of(purchaseService.purchase(userId, request, idempotencyKey));
    }
}
