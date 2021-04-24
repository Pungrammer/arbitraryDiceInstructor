package org.pungrammer.arbitrarydiceinstructor.command;

import org.pungrammer.arbitrarydiceinstructor.config.Config;

import java.time.Duration;
import java.time.Instant;

public abstract class Command {

    protected Config config;

    public Command(final Config config) {
        this.config = config;
    }

    public void execute(final String commandArgs) {
        Instant start = Instant.now();
        executeCommand(commandArgs);
        System.out.printf("command took %dms%n", Duration.between(start, Instant.now()).toMillis());
    }

    protected abstract void executeCommand(final String commandArgs);

    protected abstract String getCommandWord();
}
