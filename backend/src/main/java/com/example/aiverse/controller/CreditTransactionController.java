package com.example.aiverse.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.aiverse.common.response.PageResponse;
import com.example.aiverse.dto.CreditTransactionResponse;
import com.example.aiverse.entity.CreditTransactionType;
import com.example.aiverse.service.CreditTransactionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Credit")
@RestController
@RequestMapping("/api/credit-transactions")
@RequiredArgsConstructor
public class CreditTransactionController {

    private final CreditTransactionService creditTransactionService;

    @Operation(summary = "크레딧 거래 이력 조회")
    @SecurityRequirement(name = "bearer-jwt")
    @GetMapping
    public PageResponse<CreditTransactionResponse> search(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) CreditTransactionType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return creditTransactionService.search(userId, type, page, size);
    }
}
