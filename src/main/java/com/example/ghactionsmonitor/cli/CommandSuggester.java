package com.example.ghactionsmonitor.cli;

import org.apache.commons.text.similarity.FuzzyScore;

import java.util.List;
import java.util.Locale;

public class CommandSuggester {
    private final List<String> availableCommands;
    private final FuzzyScore fuzzyScore;

    public CommandSuggester(List<String> availableCommands) {
        this.availableCommands = availableCommands;
        this.fuzzyScore = new FuzzyScore(Locale.ENGLISH);
    }

    public String suggestCommand(String input) {
        String bestMatch = null;
        int bestMatchScore = -1;
        for (String command : availableCommands) {
            int score = fuzzyScore.fuzzyScore(command, input);
            if (score > bestMatchScore) {
                bestMatchScore = score;
                bestMatch = command;
            }
        }
        if (bestMatchScore > 0 && !bestMatch.equalsIgnoreCase(input)) {
            return bestMatch;
        } else {
            return null;
        }
    }

}
