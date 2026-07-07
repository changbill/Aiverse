package com.example.aiverse.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.aiverse.common.response.ApiResponse;
import com.example.aiverse.dto.CreditProductResponse;
import com.example.aiverse.service.CreditProductService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Credit")
@RestController
@RequestMapping("/api/credit-products")
@RequiredArgsConstructor
public class CreditProductController {

    private final CreditProductService creditProductService;

    @Operation(summary = "충전 상품 목록")
    @GetMapping
    public ApiResponse<List<CreditProductResponse>> getProducts() {
        return ApiResponse.of(creditProductService.getActiveProducts());
    }
}
