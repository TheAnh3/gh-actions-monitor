package com.example.ghactionsmonitor.cli;

import com.example.ghactionsmonitor.service.MonitorService;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;

@Component
@Command(name = "monitor", mixinStandardHelpOptions = true,
        description = "Monitor GitHub Actions workflows in real time")
public class MonitorCommand implements Runnable {

    @Option(names = {"-r","--repo"}, description = "GitHub Repository (owner/repo)", required = true)
    private String repo;

    @Option(names = {"-t","--token"}, description = "GitHub Personal Access Token", required = true)
    private String token;

    private final MonitorService monitorService;

    public MonitorCommand(MonitorService monitorService) {
        this.monitorService = monitorService;
    }

    @Override
    public void run() {
        System.out.printf("Starting monitoring for repository: %s%n", repo);
        try {
            monitorService.startMonitoring(repo, token);
        } catch (IOException e) {
            System.out.println("Monitoring failed" + e.getMessage());
        }
    }

}