package com.example.aiverse.dto;

import com.example.aiverse.entity.CreditProduct;

public record CreditProductResponse(
        Long id, String code, String name, int creditAmount, int bonusCredit, int price, int displayOrder
) {

    public static CreditProductResponse from(CreditProduct product) {
        return new CreditProductResponse(
                product.getId(), product.getCode(), product.getName(),
                product.getCreditAmount(), product.getBonusCredit(), product.getPrice(), product.getDisplayOrder()
        );
    }
}
