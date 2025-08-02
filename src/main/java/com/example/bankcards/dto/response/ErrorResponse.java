package com.example.bankcards.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Map;

public record ErrorResponse (
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        Map<String, String> validationErrors
) {
    public ErrorResponse(HttpStatus httpStatus, String message, String path) {
        this(LocalDateTime.now(), httpStatus.value(), httpStatus.getReasonPhrase(), message, path, null);
    }

    // Конструктор для ошибок валидации
    public ErrorResponse(HttpStatus httpStatus, String message, String path, Map<String, String> validationErrors) {
        this(LocalDateTime.now(), httpStatus.value(), httpStatus.getReasonPhrase(), message, path, validationErrors);
    }
}
