package com.example.ghactionsmonitor.client;

import java.util.List;
import com.example.ghactionsmonitor.model.Status;
import com.example.ghactionsmonitor.model.Step;

import java.time.Instant;
import java.util.stream.Collectors;
import java.util.List;
import java.util.stream.Collectors;

public record GitHubStepsResponse(
        List<GitHubStep> steps
) {
    public List<Step> toSteps() {
        return steps.stream()
                .map(GitHubStep::toStep)
                .collect(Collectors.toList());
    }

    public static record GitHubStep(
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
}