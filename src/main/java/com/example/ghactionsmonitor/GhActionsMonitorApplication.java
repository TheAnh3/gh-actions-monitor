package com.example.ghactionsmonitor;

import com.example.ghactionsmonitor.cli.CliRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import picocli.CommandLine;

@SpringBootApplication
public class GhActionsMonitorApplication {

    public static void main(String[] args) {
        SpringApplication app = new  SpringApplication(GhActionsMonitorApplication.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        app.run(args);
    }

    @Bean
    CommandLineRunner picoliRunner(CommandLine.IFactory factory, CliRunner cliRunner) {
        return arags -> new CommandLine(cliRunner,factory).execute(arags);
    }
}
