package com.example.ghactionsmonitor.cli;

import lombok.Setter;
import org.jline.reader.LineReader;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;

@Component
@Command(name = "",
        description = "GitHub Actions interactive shell",
        subcommands = {
                MonitorCommand.class,
                ExitCommand.class,
                picocli.CommandLine.HelpCommand.class
        })
public class RootCommand implements Runnable {

    private LineReader reader;
    @Setter
    private ClearScreenCommand clearScreenCommand;

    public void setReader(LineReader reader) {
        this.reader = reader;
        if (clearScreenCommand != null) {
            clearScreenCommand.setReader(reader);
        }
    }

    @Override
    public void run() {
       printBanner();
       printIntro();
    }

    private void printBanner() {
        if (reader != null) {
            var w = reader.getTerminal().writer();
            String[] banner = {
                    "╔══════════════════════════════════════╗",
                    "║      GitHub Actions Monitor          ║",
                    "║      Watching workflows live...      ║",
                    "╚══════════════════════════════════════╝"
            };
            for (String line : banner) {
                w.println(line);
                w.flush();
                try { Thread.sleep(100); } catch (InterruptedException ignored) {}
            }
        } else {
            System.out.println("Welcome to Gh-actions-monitor!");
        }
    }

    private void printIntro() {
        if (reader != null) {
            var w = reader.getTerminal().writer();
            w.println("Type 'help' to see available commands.");
            w.println("Use 'monitor -r owner/repo -t <token>' to start monitoring.");
            w.println("Type 'exit' to quit the shell.\n");
            w.flush();
        } else {
            System.out.println("Type 'help' to see available commands.");
            System.out.println("Use 'monitor -r owner/repo -t <token>' to start monitoring.");
            System.out.println("Type 'exit' to quit the shell.\n");
        }
    }

}
