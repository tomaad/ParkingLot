package com.parkinglot.interfaces.rest;

import java.time.Instant;
import java.util.Objects;

public final class ErrorResponse {
    private final Instant timestamp;
    private final int status;
    private final String error;
    private final String message;
    private final String path;

    public ErrorResponse(final Instant timestamp,
                         final int status,
                         final String error,
                         final String message,
                         final String path) {
        this.timestamp = Objects.requireNonNull(timestamp, "timestamp must not be null");
        this.status = status;
        this.error = Objects.requireNonNull(error, "error must not be null");
        this.message = Objects.requireNonNull(message, "message must not be null");
        this.path = Objects.requireNonNull(path, "path must not be null");
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }
}
