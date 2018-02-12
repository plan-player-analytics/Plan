package com.djrapitops.plan.command.commands;

import com.google.common.collect.ImmutableSet;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.message.Message;

import java.util.Set;

/**
 * Filters out WebUser registration command logs.
 *
 * @author Rsl1122
 * @since 3.5.2
 */
public class RegisterCommandFilter extends AbstractFilter {

    private final Set<String> censoredCommands = ImmutableSet.of("/plan web register", "/plan webuser register", "/plan register");

    public void registerFilter() {
        Logger logger = (Logger) LogManager.getRootLogger();
        logger.addFilter(this);
    }

    @Override
    public Result filter(LogEvent event) {
        if (event == null) {
            return Result.NEUTRAL;
        }

        return validateMessage(event.getMessage());
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, Message msg, Throwable t) {
        return validateMessage(msg);
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String msg, Object... params) {
        return validateMessage(msg);
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, Object msg, Throwable t) {
        if (msg == null) {
            return Result.NEUTRAL;
        }

        return validateMessage(msg.toString());
    }

    private Result validateMessage(Message message) {
        if (message == null) {
            return Result.NEUTRAL;
        }

        return validateMessage(message.getFormattedMessage());
    }

    private Result validateMessage(String message) {
        if (message == null) {
            return Result.NEUTRAL;
        }

        return commandShouldBeCensored(message)
                ? Result.DENY
                : Result.NEUTRAL;
    }

    private boolean commandShouldBeCensored(String message) {
        return message != null
                && (message.toLowerCase().contains("issued server command:")
                && shouldBeCensored(message));
    }

    private boolean shouldBeCensored(String message) {
        return message != null && censoredCommands.stream().anyMatch(message::contains);
    }
}
