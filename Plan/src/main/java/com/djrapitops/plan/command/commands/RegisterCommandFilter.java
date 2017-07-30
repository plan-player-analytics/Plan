package main.java.com.djrapitops.plan.command.commands;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.message.Message;

/**
 * Filters out WebUser registration command logs.
 *
 * @author Rsl1122
 * @since 3.5.2
 */
public class RegisterCommandFilter extends AbstractFilter {

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

    private static Result validateMessage(Message message) {
        if (message == null) {
            return Result.NEUTRAL;
        }

        return validateMessage(message.getFormattedMessage());
    }

    private static Result validateMessage(String message) {
        return isSensibleCommand(message)
                ? Result.DENY
                : Result.NEUTRAL;
    }

    private static boolean isSensibleCommand(String message) {
        message = message.toLowerCase();
        return message.contains("issued server command:") && message.contains("/plan register");
    }
}
