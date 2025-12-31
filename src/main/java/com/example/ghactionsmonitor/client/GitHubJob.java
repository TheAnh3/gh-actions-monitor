package com.example.ghactionsmonitor.client;

import com.example.ghactionsmonitor.model.Job;
import com.example.ghactionsmonitor.model.Status;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;

public record GitHubJob(
        long id,
        String name,
        String status,
        String conclusion,
        String started_at,
        String completed_at,
        long run_id,
        List<GitHubStep> steps // list steps z jobu
) {


    public Job toJob() {
        return new Job(
                id,
                run_id,
                name,
                parseInstant(started_at),
                parseInstant(completed_at),
                Status.fromString(conclusion != null ? conclusion : status),
                steps != null ? steps.stream().map(GitHubStep::toStep).toList() : List.of()
        );
    }

    private Instant parseInstant(String timestamp) {
        if (timestamp == null || timestamp.isEmpty()) return null;
        try {
            return Instant.parse(timestamp);
        } catch (DateTimeParseException e) {
            System.err.println("Failed to parse timestamp: " + timestamp);
            return null;
        }
    }
}
