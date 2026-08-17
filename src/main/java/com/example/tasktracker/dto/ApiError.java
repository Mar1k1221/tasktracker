package com.example.tasktracker.dto;

import java.time.LocalDateTime;

public class ApiError {
    private final String code;
    private final String message;
    private final String path;
    private final LocalDateTime timestamp;

    public ApiError(String code, String message, String path, LocalDateTime timestamp) {
        this.code = code;
        this.message = message;
        this.path = path;
        this.timestamp = timestamp;
    }

    public String getCode() {
        return code;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getPath() {
        return path;
    }

    public String getMessage() {
        return message;
    }
}
