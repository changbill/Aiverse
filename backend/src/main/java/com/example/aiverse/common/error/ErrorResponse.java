package com.example.aiverse.common.error;

import java.util.List;

public record ErrorResponse(
        String code,
        String message,
        List<FieldErrorResponse> fieldErrors,
        String requestId
) {

    public static ErrorResponse of(ErrorCode errorCode, String requestId) {
        return of(errorCode, errorCode.message(), List.of(), requestId);
    }

    public static ErrorResponse of(
            ErrorCode errorCode,
            String message,
            List<FieldErrorResponse> fieldErrors,
            String requestId
    ) {
        return new ErrorResponse(errorCode.code(), message, List.copyOf(fieldErrors), requestId);
    }
}
