package org.pungrammer.arbitrarydiceinstructor.command;

import org.pungrammer.arbitrarydiceinstructor.config.Config;

import java.util.ArrayList;
import java.util.List;

public class SetAvailableDiceCommand extends Command {

    protected SetAvailableDiceCommand(final Config config) {
        super(config);
    }

    @Override
    protected void executeCommand(String commandArgs) {
        String sanitizedInput = commandArgs.replace(" ", "");
        String[] parts = sanitizedInput.split(",");
        List<Integer> newDice = new ArrayList<>();

        for (String part : parts) {
            newDice.add(Integer.valueOf(part));
        }

        config.setAvailableDice(newDice);
    }

    @Override
    protected String getCommandWord() {
        return "setDice";
    }
}
