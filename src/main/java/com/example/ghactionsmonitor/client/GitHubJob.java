package com.example.ghactionsmonitor.client;

import com.example.ghactionsmonitor.model.Job;
import com.example.ghactionsmonitor.model.Status;
import com.example.ghactionsmonitor.model.Step;

import java.time.Instant;
import java.util.List;

public record GitHubJob(
        long id,
        String name,
        String status,
        String conclusion,
        String started_at,
        String completed_at,
        long run_id,
        List<GitHubStep> steps
) {
    public Job toJob() {
        List<Step> mappedSteps = steps != null
                ? steps.stream().map(GitHubStep::toStep).toList()
                : List.of();

        return new Job(
                id,
                run_id,
                name,
                started_at != null ? Instant.parse(started_at) : null,
                completed_at != null ? Instant.parse(completed_at) : null,
                Status.fromString(conclusion != null ? conclusion : status),
                mappedSteps
        );
    }
}
