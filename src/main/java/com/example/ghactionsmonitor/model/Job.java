package com.example.ghactionsmonitor.model;

import java.time.Instant;

public record Job(
        long id,
        long workflowrunId,
        String name,
        Instant startedAt,
        Instant completedAt,
        Status status,
        java.util.List<Step> mappedSteps) {
}
