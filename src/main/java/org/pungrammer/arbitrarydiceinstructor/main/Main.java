package org.pungrammer.arbitrarydiceinstructor.main;

import org.pungrammer.arbitrarydiceinstructor.command.Command;
import org.pungrammer.arbitrarydiceinstructor.command.CommandParser;
import org.pungrammer.arbitrarydiceinstructor.config.Config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {

    public static void main(final String[] varargs) throws IOException {
        System.out.println("Enter help to show help. Enter exit to end the program.");

        CommandParser cp = new CommandParser(new Config());
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        while (!Thread.currentThread().isInterrupted()) {
            System.out.print("$: ");
            String rawCommand = br.readLine();
            if (rawCommand.isBlank()) continue;
            if (rawCommand.equals("exit")) {
                System.exit(0);
            }
            try {
                String commandWord = rawCommand;
                String commandArgs = null;
                if (rawCommand.contains(" ")) {
                    commandWord = rawCommand.substring(0, rawCommand.indexOf(" "));
                    commandArgs = rawCommand.substring(rawCommand.indexOf(" ") + 1);
                }


                Command command = cp.parseCommand(commandWord);
                command.execute(commandArgs);
            } catch (IllegalArgumentException e) {
                System.err.println(e.getMessage());
            }
        }
    }

}
