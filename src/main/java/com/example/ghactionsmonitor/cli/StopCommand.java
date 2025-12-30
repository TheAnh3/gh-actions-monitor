package com.example.ghactionsmonitor.cli;

import com.example.ghactionsmonitor.service.MonitorService;
import org.jline.reader.LineReader;
import org.springframework.stereotype.Component;
import picocli.CommandLine;
import picocli.CommandLine.ParentCommand;


@Component
@CommandLine.Command(name = "stop", description = "Stop monitoring the repository")
public class StopCommand implements Runnable {

    @ParentCommand
    private RootCommand parent;

    private final MonitorService monitorService;

    public StopCommand(MonitorService monitorService) {
        this.monitorService = monitorService;
    }

    @Override
    public void run() {
        LineReader reader = parent.getReader();
        if (monitorService.isRunning()) {
            monitorService.stopMonitoring();
            reader.printAbove("Monitoring stopped.");
        } else {
            reader.printAbove("Monitoring is not running.");
        }
    }
}