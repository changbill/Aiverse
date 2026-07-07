package com.example.aiverse.common.error;

import org.springframework.http.HttpStatus;

public enum AssetErrorCode implements ErrorCode {
    ASSET_NOT_FOUND("ASSET_NOT_FOUND", "콘텐츠를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    CATEGORY_NOT_FOUND("CATEGORY_NOT_FOUND", "카테고리를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);

    private final String code;
    private final String message;
    private final HttpStatus status;

    AssetErrorCode(String code, String message, HttpStatus status) {
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
