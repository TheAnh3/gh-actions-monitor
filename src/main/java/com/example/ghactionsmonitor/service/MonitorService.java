package com.example.ghactionsmonitor.service;

import com.example.ghactionsmonitor.cli.MonitorStateStore;
import com.example.ghactionsmonitor.client.GitHubClient;
import com.example.ghactionsmonitor.client.GitHubJob;
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
                                    List<Event> events = collectEventsForRun(owner, repoName, run, token);
                                    for (Event event : events) {
                                        reader.printAbove(formatEvent(event));
                                    }
                                    stateStore.updateLastSeen(repoName, run.id());
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


    private List<Event> collectEventsForRun(String owner, String repo, WorkflowRun run, String token) {
        List<Event> events = new ArrayList<>();

        // 1️⃣ Workflow event
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
                run.name(),
                run.id(),
                run.branch(),
                run.commitSHA(),
                run.actorLogin(),
                null,                       // jobName
                run.completedAt()            // completedAt
        ));

        // 2️⃣ Jobs + steps
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
                    run.name(),
                    job.id(),
                    null,
                    null,
                    null,
                    null,
                    job.completedAt()          // completedAt
            ));

            // Steps
            GitHubJob ghJob = gitHubClient.getGitHubJob(owner, repo, job.id(), token);
            if (ghJob != null && ghJob.steps() != null) {
                for (var step : ghJob.steps()) {
                    Status stepStatus = Status.fromString(step.conclusion() != null ? step.conclusion() : step.status());
                    EventType stepEvent = switch (stepStatus) {
                        case IN_PROGRESS -> EventType.STEP_STARTED;
                        case SUCCESS, FAILURE, CANCELED -> EventType.STEP_COMPLETED;
                        default -> EventType.STEP_STARTED;
                    };

                    events.add(new Event(
                            stepEvent,
                            step.started_at() != null ? Instant.parse(step.started_at()) : Instant.now(),
                            EntityType.STEP,
                            step.name(),
                            stepStatus,
                            run.name(),
                            null,
                            null,
                            null,
                            null,
                            job.name(),
                            step.completed_at() != null ? Instant.parse(step.completed_at()) : null
                    ));
                }
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
        String timestamp = event.timestamp() != null ? event.timestamp().toString() : "?";

        return switch (event.entityType()) {
            case WORKFLOW -> String.format(
                    "[%s] WORKFLOW | ID: %d | Name: %s | Branch: %s | Commit: %s | Status: %s | Actor: %s | Started: %s | Completed: %s",
                    timestamp,
                    event.id() != null ? event.id() : -1,
                    event.entityName(),
                    event.branch() != null ? event.branch() : "?",
                    event.commit() != null ? event.commit() : "?",
                    event.status(),
                    event.actor() != null ? event.actor() : "?",
                    event.timestamp(),                  // start
                    event.completedAt() != null ? event.completedAt() : "?" // completion
            );
            case JOB -> String.format(
                    "[%s] JOB      | ID: %d | Name: %s | Status: %s | Workflow: %s | Started: %s | Completed: %s",
                    timestamp,
                    event.id() != null ? event.id() : -1,
                    event.entityName(),
                    event.status(),
                    event.workflowName(),
                    event.timestamp(),                  // start
                    event.completedAt() != null ? event.completedAt() : "?" // completion
            );
            case STEP -> String.format(
                    "[%s] STEP     | Name: %s | Status: %s | Job: %s | Workflow: %s | Started: %s | Completed: %s",
                    timestamp,
                    event.entityName(),
                    event.status(),
                    event.jobName(),
                    event.workflowName(),
                    event.timestamp(),                  // start
                    event.completedAt() != null ? event.completedAt() : "?" // completion
            );
        };
    }




}
