package main.java.com.djrapitops.plan.command;

/**
 * This enum contains different types of commands.
 *
 * CONSOLE can be used always, PLAYER can only be used as a player,
 * CONSOLE_WITH_ARGUMENTS can be used always, except with arguments on console.
 *
 * @author Rsl1122
 */
public enum CommandType {
    CONSOLE,
    PLAYER,
    CONSOLE_WITH_ARGUMENTS
}
