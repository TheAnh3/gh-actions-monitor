package com.example.ghactionsmonitor.client;
import com.example.ghactionsmonitor.model.Job;

import java.util.List;
import java.util.stream.Collectors;

public record GitHubJobsResponse(
        Integer total_count,
        List<GitHubJob> jobs
) {
    public List<Job> toJobs(long workflowRunId) {
        if (jobs == null) return List.of();
        return jobs.stream()
                .map(job -> job.toJob(workflowRunId))
                .collect(Collectors.toList());
    }
}