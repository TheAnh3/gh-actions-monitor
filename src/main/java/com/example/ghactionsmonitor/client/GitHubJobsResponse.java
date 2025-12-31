package com.example.ghactionsmonitor.client;
import com.example.ghactionsmonitor.model.Job;

import java.util.List;
import java.util.stream.Collectors;

public record GitHubJobsResponse(
        int total_count,
        List<GitHubJob> jobs
) {
    public List<Job> toJobs() {
        return jobs.stream().map(GitHubJob::toJob).collect(Collectors.toList());
    }
}