package com.example.ghactionsmonitor.cli;

import org.jline.reader.*;
import org.jline.reader.impl.DefaultParser;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.widget.TailTipWidgets;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import picocli.CommandLine;
import picocli.shell.jline3.PicocliCommands.PicocliCommandsFactory;
import picocli.shell.jline3.PicocliCommands;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Component
public class ShellRunner implements CommandLineRunner {

    private final RootCommand rootCommand;
    List<String> commands = List.of("monitor", "exit", "clear", "help");


    public ShellRunner(RootCommand rootCommand) {
        this.rootCommand = rootCommand;
    }

    @Override
    public void run(String... args) throws Exception {
        Path workDir = Paths.get(System.getProperty("user.dir"));

        Terminal terminal = TerminalBuilder.builder().system(true).build();

        PicocliCommandsFactory factory = new PicocliCommandsFactory();
        CommandLine cmd = new CommandLine(rootCommand, factory);
        PicocliCommands picocliCommands = new PicocliCommands(cmd);

        Parser parser = new DefaultParser();
        org.jline.console.SystemRegistry systemRegistry =
                new org.jline.console.impl.SystemRegistryImpl(parser, terminal, () -> workDir, null);
        systemRegistry.setCommandRegistries(picocliCommands);

        LineReader reader = LineReaderBuilder.builder()
                .terminal(terminal)
                .completer(systemRegistry.completer())
                .parser(parser)
                .variable(LineReader.LIST_MAX, 50)
                .build();

        rootCommand.setReader(reader);
        ClearScreenCommand clearCmd = new ClearScreenCommand();
        clearCmd.setReader(reader);
        rootCommand.setClearScreenCommand(clearCmd);
        cmd.addSubcommand("clear", clearCmd);
        rootCommand.run();

        TailTipWidgets widgets = new TailTipWidgets(reader, systemRegistry::commandDescription, 5,
                TailTipWidgets.TipType.COMPLETER);
        widgets.enable();

        String prompt = "ghshell> ";

        while (true) {
            try {
                String line = reader.readLine(prompt);
                String commandName = line.split("\\s+")[0]; // entered command

                if (!commands.contains(commandName)) {
                    var w = reader.getTerminal().writer();
                    w.println("Unknown command: " + commandName);
                    CommandSuggester suggester = new CommandSuggester(commands);
                    String suggestion = suggester.suggestCommand(commandName);
                    if (suggestion != null) {
                        w.println("Did you mean: " + suggestion + "?");
                    } else {
                        w.println("Type 'help' to see available commands.");
                    }
                    w.flush();
                }else {
                    systemRegistry.execute(line);
                }
            } catch (UserInterruptException e) {
                // CTRL C Ignored
            } catch (EndOfFileException e) {
                break; // CTRL-D: ends process
            } catch (Exception e) {

          }
        }

    }
}
