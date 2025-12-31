package com.example.ghactionsmonitor.client;

import com.example.ghactionsmonitor.model.Status;
import com.example.ghactionsmonitor.model.Step;

import java.time.Instant;

public record GitHubStep(
        String name,
        int number,
        String status,
        String conclusion,
        String started_at,
        String completed_at
) {
    public Step toStep() {
        return new Step(
                name,
                number,
                started_at != null ? Instant.parse(started_at) : null,
                completed_at != null ? Instant.parse(completed_at) : null,
                Status.fromString(conclusion != null ? conclusion : status)
        );
    }
}