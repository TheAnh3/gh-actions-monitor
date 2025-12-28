package com.example.ghactionsmonitor.client;

import com.example.ghactionsmonitor.model.Status;
import com.example.ghactionsmonitor.model.WorkflowRun;

import java.time.Instant;
import java.time.format.DateTimeParseException;

public record GitHubWorkflowRun(
        long id,
        String name,
        String head_branch,
        String head_sha,
        String status,
        String conclusion,
        String created_at,
        String updated_at,
        GitHubUser actor
) {

    public WorkflowRun toWorkflowRun() {
        Instant started = null;
        Instant completed = null;

        try {
            if (created_at != null) started = Instant.parse(created_at);
            if (updated_at != null) completed = Instant.parse(updated_at);
        } catch (DateTimeParseException e) {
            e.printStackTrace();
        }

        return new WorkflowRun(
                id,
                name,
                head_branch,
                head_sha,
                started,
                completed,
                Status.fromString(conclusion != null ? conclusion : status),
                actor != null ? actor.login() : null
        );
    }
}
