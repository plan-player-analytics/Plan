package main.java.com.djrapitops.plan.command.commands;

import java.util.logging.Filter;
import java.util.logging.LogRecord;

/**
 * Filters out WebUser registration command logs.
 *
 * @author Rsl1122
 * @since 3.5.2
 */
public class RegisterCommandFilter implements Filter {

    @Override
    public boolean isLoggable(LogRecord record) {
        String message = record.getMessage();
        boolean block = message.contains("command: /plan register")
                || message.contains("command: /plan web register")
                || message.contains("command: /plan webuser register");
        return !block;
    }
}
