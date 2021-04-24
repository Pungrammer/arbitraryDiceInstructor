package org.pungrammer.arbitrarydiceinstructor.command;

import org.pungrammer.arbitrarydiceinstructor.config.Config;

public class SetMaxRollCount extends Command {

    public SetMaxRollCount(final Config config) {
        super(config);
    }

    @Override
    protected void executeCommand(String commandArgs) {
        int newMaxCount = Integer.parseInt(commandArgs);

        if (newMaxCount < 1) {
            throw new IllegalArgumentException("Roll count must be 1 or higher.");
        }

        if (newMaxCount > 5) {
            System.out.println("It is highly discouraged to increase this value above 5.\n" +
                    "If you reconsider, call this command again with a lower value.");
        }

        config.setMaxRollCount(newMaxCount);
    }

    @Override
    protected String getCommandWord() {
        return "setMaxRollCount";
    }
}
