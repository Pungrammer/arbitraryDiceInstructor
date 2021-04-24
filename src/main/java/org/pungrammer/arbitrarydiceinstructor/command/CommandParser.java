package org.pungrammer.arbitrarydiceinstructor.command;

import org.pungrammer.arbitrarydiceinstructor.config.Config;

import java.util.HashSet;
import java.util.Set;

public class CommandParser {


    private final Set<Command> availableCommands;

    public CommandParser(final Config config) {
        availableCommands = new HashSet<>();
        availableCommands.add(new SetAvailableDiceCommand(config));
        availableCommands.add(new DiceInstructionCommand(config));
        availableCommands.add(new HelpCommand(config));
    }

    public Command parseCommand(final String commandWord) {
        for (Command availableCommand : availableCommands) {
            if (availableCommand.getCommandWord().equals(commandWord)) {
                return availableCommand;
            }
        }

        throw new IllegalArgumentException("Unknown command word");
    }
}
