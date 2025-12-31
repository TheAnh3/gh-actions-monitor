package com.example.ghactionsmonitor.model;

import java.time.Instant;
public record Event(
        EventType eventType,
        Instant timestamp,
        EntityType entityType,
        String entityName,
        Status status,
        String workflowName,
        Long id,
        String branch,
        String commit,
        String actor,
        String jobName,
        Instant completedAt
) {}