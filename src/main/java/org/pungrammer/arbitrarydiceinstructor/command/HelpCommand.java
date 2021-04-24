package org.pungrammer.arbitrarydiceinstructor.command;

import org.pungrammer.arbitrarydiceinstructor.config.Config;

public class HelpCommand extends Command {

    protected HelpCommand(final Config config) {
        super(config);
    }

    @Override
    protected void executeCommand(String commandArgs) {
        System.out.println("" +
                "Available commands:\n" +
                "calc <wantedDie>          : calculates the steps to simulate this die using the available dies\n" +
                "setDice <die1, die2, ...> : sets the available dies. List is a comma separated list of integers\n" +
                "                          : Duplicates and spaces are ignored\n" +
                "help                      : Shows this help");
    }

    @Override
    protected String getCommandWord() {
        return "help";
    }
}
