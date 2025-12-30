package com.example.ghactionsmonitor.cli;

import com.example.ghactionsmonitor.service.MonitorService;
import org.jline.reader.LineReader;
import org.springframework.stereotype.Component;
import picocli.CommandLine;
import picocli.CommandLine.ParentCommand;


@Component
@CommandLine.Command(name = "status", description = "Show monitoring status")
public class StatusCommand implements Runnable {

    @ParentCommand
    private RootCommand parent;

    private final MonitorService monitorService;

    public StatusCommand(MonitorService monitorService) {
        this.monitorService = monitorService;
    }

    @Override
    public void run() {
        LineReader reader = parent.getReader();
        String msg = monitorService.isRunning() ? "Monitoring is running." : "Monitoring is not running.";
        if (reader != null) reader.printAbove(msg);
        else System.out.println(msg);
    }
}