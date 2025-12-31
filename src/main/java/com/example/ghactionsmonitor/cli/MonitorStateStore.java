package com.example.ghactionsmonitor.cli;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@Getter
@Setter
public class MonitorStateStore {

    private final File file;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private Map<String, Long> lastSeenWorkflowRuns = new HashMap<>();

    public MonitorStateStore() {
        this.file = new File("./monitor_state.json");
        init();
    }

    private void init() {
        if (file.exists()) {
            try {
                lastSeenWorkflowRuns = objectMapper.readValue(
                        file, new TypeReference<Map<String, Long>>() {});
            } catch (IOException e) {
                System.err.println("Cannot read monitor state file, starting fresh.");
            }
        } else {
            File parent = file.getParentFile();
            if (parent != null && parent.mkdirs()) {
                System.out.println("Created directory for monitor state: " + parent.getAbsolutePath());
            }
            try {
                file.createNewFile();
            } catch (IOException e) {
                System.err.println("Cannot create monitor state file: " + e.getMessage());
            }
        }
    }

    public synchronized void updateLastSeen(String repo, long workflowId) {
        lastSeenWorkflowRuns.put(repo, workflowId);
        save();
    }

    public synchronized Long getLastSeen(String repo) {
        return lastSeenWorkflowRuns.get(repo);
    }

    public synchronized void resetLastSeen(String repo) {
        lastSeenWorkflowRuns.remove(repo);
        save();
    }

    private void save() {
        try {
            File tempFile = new File(file.getAbsolutePath() + ".tmp");
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(tempFile, lastSeenWorkflowRuns);
            if (!tempFile.renameTo(file)) {
                System.err.println("Cannot rename temp file to monitor state file");
            }
        } catch (IOException e) {
            System.err.println("Cannot save monitor state: " + e.getMessage());
        }
    }
}

