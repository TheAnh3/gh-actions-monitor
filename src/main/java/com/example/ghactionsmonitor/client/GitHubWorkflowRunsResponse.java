package com.example.ghactionsmonitor.client;

import com.example.ghactionsmonitor.model.WorkflowRun;

import java.util.List;
import java.util.stream.Collectors;

public record GitHubWorkflowRunsResponse(
        Integer total_count,
        List<GitHubWorkflowRun> workflow_runs
) {
    public List<WorkflowRun>toWorkflowRuns() {
        return workflow_runs.stream().map(GitHubWorkflowRun::toWorkflowRun).collect(Collectors.toList());
    }
}
