package com.example.ghactionsmonitor.model;

import java.time.Instant;

public record WorkflowRun(
        long id,
        String name,
        String branch,
        String commitSHA,
        Instant startedAt,
        Instant completedAt,
        Status status,
        String actorLogin
) {
}
