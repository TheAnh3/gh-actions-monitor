package com.example.ghactionsmonitor.model;

import java.time.Instant;

public record Step(
        String name,
        int number,
        Instant startedAt,
        Instant compledAt,
        Status status
) {
}
