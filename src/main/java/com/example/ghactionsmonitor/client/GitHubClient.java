package com.example.ghactionsmonitor.client;

import com.example.ghactionsmonitor.model.Job;
import com.example.ghactionsmonitor.model.Step;
import com.example.ghactionsmonitor.model.WorkflowRun;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

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

    public List<Job> listJobs(String owner, String repo, long runId, String token) {
        try {
            GitHubJobsResponse response = webClient.get()
                    .uri("/repos/{owner}/{repo}/actions/runs/{run_id}/jobs", owner, repo, runId)
                    .headers(h -> h.setBearerAuth(token))
                    .retrieve()
                    .bodyToMono(GitHubJobsResponse.class)
                    .block();

            return response != null ? response.toJobs() : List.of();
        } catch (WebClientResponseException e) {
            System.out.println("Github API Error: " +  e.getStatusCode() + " - " + e.getResponseBodyAsString());
            return List.of();
        }
    }

    public List<Step> listSteps(String owner, String repo, long runId, long jobId, String token) {
        try {
            GitHubStepsResponse response = webClient.get()
                    .uri("/repos/{owner}/{repo}/actions/jobs/{job_id}/steps", owner, repo, jobId)
                    .headers(h -> h.setBearerAuth(token))
                    .retrieve()
                    .bodyToMono(GitHubStepsResponse.class)
                    .block();

            return response != null ? response.toSteps() : List.of();

        } catch (WebClientResponseException e) {
            System.out.println("GitHub API Error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            return List.of();
        }
    }
}
