package com.example.ghactionsmonitor.cli;

import lombok.Setter;
import org.jline.reader.LineReader;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;

@Setter
@Component
@Command(name = "clear", description = "Clear the terminal screen")
public class ClearScreenCommand implements Runnable {

    private LineReader reader;

    @Override
    public void run() {
        if (reader != null) {
            try {
                reader.getTerminal().puts(org.jline.utils.InfoCmp.Capability.clear_screen);
                reader.getTerminal().flush();
            } catch (Exception e) {
                System.out.println("Clearing failed: " +  e.getMessage());
            }
        } else {
            System.out.println("Cannot clear screen: terminal not available.");
        }
    }
}
