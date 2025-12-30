package com.example.ghactionsmonitor.cli;

import com.example.ghactionsmonitor.service.MonitorService;
import org.springframework.stereotype.Component;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;


@Component
@Command(name = "gh-monitor", mixinStandardHelpOptions = true, version = "1.0",
        description = "Monitor GitHub Actions workflows in real time")
public class CliRunner implements Runnable {

    @Option(names = {"-r","--repo"}, description = "GitHub Repository (owner/repo)", required = true)
    private String repo;

    @Option(names = {"-t", "--token"}, description = "Github Personal Access Token",  required = true)
    private String token;

    private final MonitorService monitorService;

    public CliRunner(MonitorService monitorService) {
        this.monitorService = monitorService;
    }

    @Override
    public void run() {
        printBanner();
        printIntro();
        //System.out.println("Repo: " + repo);
        //System.out.println("Token: " + token);
        monitorService.startMonitoring(repo,token);
    }

    private void printBanner() {
        System.out.println("""
        ╔══════════════════════════════════════╗
        ║      GitHub Actions Monitor          ║
        ║      Watching workflows live...      ║
        ╚══════════════════════════════════════╝
        """);
    }

    private void printIntro() {
        System.out.printf("""
                Monitoring repository: %s
                Press Ctrl+C to stop monitoring
                Use --help to see all options%n""", repo);
    }

}
