package com.example.aiverse.common.error;

import org.springframework.http.HttpStatus;

public enum AssetErrorCode implements ErrorCode {
    ASSET_NOT_FOUND("ASSET_NOT_FOUND", "콘텐츠를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    CATEGORY_NOT_FOUND("CATEGORY_NOT_FOUND", "카테고리를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    FORBIDDEN("FORBIDDEN", "본인이 등록한 콘텐츠만 수정·삭제할 수 있습니다.", HttpStatus.FORBIDDEN),
    ALREADY_SOLD("ALREADY_SOLD", "이미 판매된 콘텐츠는 원본·라이선스를 변경할 수 없습니다.", HttpStatus.CONFLICT);

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
