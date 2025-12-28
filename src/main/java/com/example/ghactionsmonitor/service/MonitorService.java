package com.example.ghactionsmonitor.service;

import com.example.ghactionsmonitor.client.GitHubClient;
import com.example.ghactionsmonitor.model.WorkflowRun;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MonitorService {
    private final GitHubClient gitHubClient;

    MonitorService(GitHubClient gitHubClient){
        this.gitHubClient = gitHubClient;
    }


    public void startMonitoring(String repo, String token) {
        String[] parts = repo.split("/");
        if (parts.length != 2) {
            System.err.println("Repo musí být ve formátu owner/repo");
            return;
        }
        String owner = parts[0];
        String repoName = parts[1];

        List<WorkflowRun> runs = gitHubClient.listWorkflowRuns(owner, repoName, token);
        if (runs.isEmpty()) {
            System.out.println("No Workflow Runs found");
        } else {
            System.out.println("WorkflowRuns Found:");
            runs.forEach(run -> System.out.printf(
                    "ID: %d | Name: %s | Branch: %s | Commit: %s | Status: %s | Actor: %s | Started: %s | Completed: %s%n",
                    run.id(), run.name(), run.branch(), run.commitSHA(), run.status(), run.actorLogin(),
                    run.startedAt(), run.completedAt()
            ));
        }
    }
}
