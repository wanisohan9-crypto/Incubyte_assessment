package com.acme.salary_management.web.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        List<String> messages) {

    public static ErrorResponse of(int status, String error, List<String> messages) {
        return new ErrorResponse(LocalDateTime.now(), status, error, messages);
    }
}
