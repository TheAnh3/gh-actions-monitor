package com.example.ghactionsmonitor.cli;

import com.example.ghactionsmonitor.service.MonitorService;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;


@Component
@Command(name = "exit", description = "Stops monitoring")
public class ExitCommand implements Runnable {
    private final MonitorService monitorService;

    public ExitCommand(MonitorService monitorService) {
        this.monitorService = monitorService;
    }
    @Override
    public void run() {
        System.out.println("Stop monitoring...");
        System.out.println("Saving last reported workflow run from GitHub");
        monitorService.stopMonitoring();
    }
}
