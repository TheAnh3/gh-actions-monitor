package com.example.ghactionsmonitor.service;

import com.example.ghactionsmonitor.cli.MonitorStateStore;
import com.example.ghactionsmonitor.client.GitHubClient;
import com.example.ghactionsmonitor.model.WorkflowRun;
import lombok.Getter;
import org.jline.reader.LineReader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@Service
public class MonitorService {
    private final GitHubClient gitHubClient;
    private final MonitorStateStore stateStore;


    MonitorService(GitHubClient gitHubClient, MonitorStateStore stateStore){
        this.gitHubClient = gitHubClient;
        this.stateStore = stateStore;
    }
    @Getter
    boolean running = false;
    private Thread MonitoringThread;


    public synchronized void startMonitoring(String repo, String token, LineReader reader) throws IOException {
        if (running) {
            reader.printAbove("Monitoring already running.");
            return;
        }

        String[] parts = repo.split("/");
        if (parts.length != 2) {
            reader.printAbove("Must be in format: owner/repo");
            return;
        }
        String owner = parts[0];
        String repoName = parts[1];
        running = true;

        MonitoringThread = new Thread(() -> {
            while (running) {
                try {
                    List<WorkflowRun> runs = gitHubClient.listWorkflowRuns(owner, repoName, token);
                    if (runs.isEmpty()) {
                        reader.printAbove("No Workflow Runs found");
                    } else {
                        Long lastSeen = stateStore.getLastSeen(repo);
                        boolean firstRun = lastSeen == null;
                        Instant oneHourAgo = Instant.now().minusSeconds(3600);

                        runs.stream()
                                .filter(run -> firstRun ? run.startedAt().isAfter(oneHourAgo) : run.id() > lastSeen)
                                .sorted(Comparator.comparingLong(WorkflowRun::id))
                                .forEach(run -> {
                            String line = String.format(
                                    "ID: %d | Name: %s | Branch: %s | Commit: %s | Status: %s | Actor: %s | Started: %s | Completed: %s",
                                    run.id(), run.name(), run.branch(), run.commitSHA(), run.status(), run.actorLogin(),
                                    run.startedAt(), run.completedAt()
                            );
                            reader.printAbove(line);
                            stateStore.updateLastSeen(repo, run.id());
                             });
                    }
                    Thread.sleep(15 * 1000L);
                } catch (InterruptedException e) {
                    if (!running) {
                        break;
                    }
                } catch (Exception e) {
                    System.out.println("Exception while fetching workflow runs: " + e.getMessage());
                }
            }

            System.out.println("Monitoring stopped.");
        }, "monitor-thread");
        MonitoringThread.start();
    }

    public void stopMonitoring() {
        if (!running) {
            System.out.println("Monitoring is not running.");
            return;
        }

        running = false;
        MonitoringThread.interrupt();
    }

}
