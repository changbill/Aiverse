package com.example.aiverse.common.error;

import org.springframework.http.HttpStatus;

public enum PurchaseErrorCode implements ErrorCode {
    SELF_PURCHASE_NOT_ALLOWED("SELF_PURCHASE_NOT_ALLOWED", "본인이 등록한 콘텐츠는 구매할 수 없습니다.", HttpStatus.CONFLICT),
    ALREADY_PURCHASED("ALREADY_PURCHASED", "이미 구매한 콘텐츠입니다.", HttpStatus.CONFLICT),
    INSUFFICIENT_BALANCE("INSUFFICIENT_BALANCE", "크레딧 잔액이 부족합니다.", HttpStatus.CONFLICT),
    PURCHASE_NOT_FOUND("PURCHASE_NOT_FOUND", "구매 내역을 찾을 수 없습니다.", HttpStatus.NOT_FOUND);

    private final String code;
    private final String message;
    private final HttpStatus status;

    PurchaseErrorCode(String code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }

    @Override
    public HttpStatus status() {
        return status;
    }
}
