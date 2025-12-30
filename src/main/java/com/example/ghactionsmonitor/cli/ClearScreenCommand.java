package com.example.ghactionsmonitor.cli;

import lombok.Setter;
import org.jline.reader.LineReader;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

@Setter
@Component
@Command(name = "clear", description = "Clear the terminal screen")
public class ClearScreenCommand implements Runnable {

    @ParentCommand
    private RootCommand parent;

    //LineReader reader = parent.getReader();


    @Override
    public void run() {
        if (parent.getReader() != null) {
            try {
                LineReader reader = parent.getReader();
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
