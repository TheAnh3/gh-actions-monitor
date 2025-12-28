package com.example.ghactionsmonitor.model;

public enum Status {
    SUCCESS,
    FAILURE,
    CANCELED,
    IN_PROGRESS;

    public static Status fromString(String value) {
        if (value == null) return null;
        return switch (value.toLowerCase()) {
            case "success", "completed" -> SUCCESS;
            case "failure" -> FAILURE;
            case "cancelled", "canceled" -> CANCELED;
            case "in_progress", "queued", "pending" -> IN_PROGRESS;
            default -> IN_PROGRESS;
        };
    }
}
