package com.example.aiverse.common.error;

import org.springframework.http.HttpStatus;

public enum FileErrorCode implements ErrorCode {
    INVALID_FILE_FORMAT("INVALID_FILE_FORMAT", "허용되지 않는 파일 형식입니다.", HttpStatus.BAD_REQUEST),
    FILE_TOO_LARGE("FILE_TOO_LARGE", "허용된 최대 파일 크기를 초과했습니다.", HttpStatus.BAD_REQUEST),
    OBJECT_VERIFICATION_FAILED("OBJECT_VERIFICATION_FAILED", "업로드된 객체를 확인할 수 없습니다.", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus status;

    FileErrorCode(String code, String message, HttpStatus status) {
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
