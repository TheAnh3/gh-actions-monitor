package com.example.ghactionsmonitor.client;

import com.example.ghactionsmonitor.model.Job;
import com.example.ghactionsmonitor.model.Step;
import com.example.ghactionsmonitor.model.WorkflowRun;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class GitHubClient {
    private final WebClient webClient;

    public GitHubClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://api.github.com").build();
    }
        public List<WorkflowRun> listWorkflowRuns(String owner, String repo, String token){
            try{
                GitHubWorkflowRunsResponse response = webClient.get()
                        .uri("/repos/{owner}/{repo}/actions/runs",owner,repo)
                        .headers(headers -> headers.setBearerAuth(token))
                        .retrieve()
                        .bodyToMono(GitHubWorkflowRunsResponse.class)
                        .block();

                return response != null ? response.toWorkflowRuns() : List.of();

            }catch (WebClientResponseException e){
                System.out.println("Github API Error: " +  e.getStatusCode() + " - " + e.getResponseBodyAsString());
                return List.of();
            }


    }


    // 2️⃣ Seznam jobů pro workflow run
    public List<Job> listJobs(String owner, String repo, long runId, String token) {
        try {
            GitHubJobsResponse response = webClient.get()
                    .uri("/repos/{owner}/{repo}/actions/runs/{run_id}/jobs", owner, repo, runId)
                    .headers(h -> h.setBearerAuth(token))
                    .retrieve()
                    .bodyToMono(GitHubJobsResponse.class)
                    .block();

            return response != null ? response.toJobs(runId) : List.of();
        } catch (WebClientResponseException e) {
            System.out.println("GitHub Jobs Error for runId=" + runId + ": " + e.getStatusCode());
            return List.of();
        }
    }

    public GitHubJob getGitHubJob(String owner, String repo, long jobId, String token) {
        try {
            GitHubJobsResponse response = webClient.get()
                    .uri("/repos/{owner}/{repo}/actions/jobs/{job_id}", owner, repo, jobId)
                    .headers(h -> h.setBearerAuth(token))
                    .retrieve()
                    .bodyToMono(GitHubJobsResponse.class)
                    .block();

            if (response != null && response.jobs() != null && !response.jobs().isEmpty()) {
                return response.jobs().get(0);
            } else {
                return null;
            }
        } catch (WebClientResponseException e) {
            System.out.println("GitHub Job Error for jobId=" + jobId + ": " + e.getStatusCode());
            return null;
        }
    }

    // 4️⃣ Získání steps přímo z jobu
    public List<Step> listStepsFromJob(GitHubJob job) {
        if (job == null || job.steps() == null) return List.of();
        return job.steps().stream()
                .map(GitHubStep::toStep)
                .collect(Collectors.toList());
    }
}