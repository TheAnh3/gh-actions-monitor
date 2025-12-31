package com.example.ghactionsmonitor.service;

import com.example.ghactionsmonitor.cli.MonitorStateStore;
import com.example.ghactionsmonitor.client.GitHubClient;
import com.example.ghactionsmonitor.model.*;
import lombok.Getter;
import org.jline.reader.LineReader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
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
                                    // Listing workflow + jobs + steps
                                    List<Event> events = collectEvents(owner, repo, run, token);

                                    // formatting
                                    for (Event event : events) {
                                        String formatted = formatEvent(event);
                                        reader.printAbove(formatted);
                                    }

                                    // update last reported workflow
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


    private List<Event> collectEvents(String owner, String repo, WorkflowRun run, String token) {
        List<Event> events = new ArrayList<>();

        // Workflow events
        EventType workflowEvent = switch (run.status()) {
            case IN_PROGRESS -> EventType.WORKFLOW_STARTED;
            case SUCCESS, FAILURE, CANCELED -> EventType.WORKFLOW_COMPLETED;
            default -> EventType.WORKFLOW_QUEUED;
        };
        events.add(new Event(
                workflowEvent,
                run.startedAt() != null ? run.startedAt() : Instant.now(),
                EntityType.WORKFLOW,
                run.name(),
                run.status(),
                "Branch: " + run.branch() + ", Commit: " + run.commitSHA()
        ));

        // Jobs
        List<Job> jobs = gitHubClient.listJobs(owner, repo, run.id(), token);
        for (Job job : jobs) {
            EventType jobEvent = switch (job.status()) {
                case IN_PROGRESS -> EventType.JOB_STARTED;
                case SUCCESS, FAILURE, CANCELED -> EventType.JOB_COMPLETED;
                default -> EventType.JOB_STARTED;
            };
            events.add(new Event(
                    jobEvent,
                    job.startedAt() != null ? job.startedAt() : Instant.now(),
                    EntityType.JOB,
                    job.name(),
                    job.status(),
                    "Workflow: " + run.name()
            ));

            // Steps
            List<Step> steps = gitHubClient.listSteps(owner, repo, run.id(), job.id(), token);
            for (Step step : steps) {
                EventType stepEvent = switch (step.status()) {
                    case IN_PROGRESS -> EventType.STEP_STARTED;
                    case SUCCESS, FAILURE, CANCELED -> EventType.STEP_COMPLETED;
                    default -> EventType.STEP_STARTED;
                };
                events.add(new Event(
                        stepEvent,
                        step.startedAt() != null ? step.startedAt() : Instant.now(),
                        EntityType.STEP,
                        step.name(),
                        step.status(),
                        "Job: " + job.name()
                ));
            }
        }

        return events;
    }

    public void stopMonitoring() {
        if (!running) {
            System.out.println("Monitoring is not running.");
            return;
        }

        running = false;
        MonitoringThread.interrupt();
    }
    private String formatEvent(Event event) {
        return switch (event.entityType()) {
            case WORKFLOW -> String.format(
                    "[%s] WORKFLOW | ID: %s | Name: %s | Branch: %s | Commit: %s | Status: %s | Actor: %s",
                    event.timestamp(),
                    event.details().contains("ID:") ? event.details().split("ID:")[1].trim() : "?",
                    event.entityName(),
                    event.details().contains("Branch:") ? event.details().split("Branch:")[1].split(",")[0].trim() : "?",
                    event.details().contains("Commit:") ? event.details().split("Commit:")[1].trim() : "?",
                    event.status(),
                    event.details().contains("Actor:") ? event.details().split("Actor:")[1].trim() : "?"
            );
            case JOB -> String.format(
                    "[%s] JOB | Name: %s | Status: %s | Workflow: %s",
                    event.timestamp(),
                    event.entityName(),
                    event.status(),
                    event.details().replace("Workflow: ", "")
            );
            case STEP -> String.format(
                    "[%s] STEP | Name: %s | Status: %s | Job: %s",
                    event.timestamp(),
                    event.entityName(),
                    event.status(),
                    event.details().replace("Job: ", "")
            );
        };
    }


}
