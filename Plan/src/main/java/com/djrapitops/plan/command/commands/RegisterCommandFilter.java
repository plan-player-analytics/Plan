package main.java.com.djrapitops.plan.command.commands;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.message.Message;

/**
 * Filters out WebUser registration command logs.
 *
 * @author Rsl1122
 * @since 3.5.2
 */
public class RegisterCommandFilter implements Filter {

    private boolean start = true;

    private Result filter(String message) {
        boolean block = message.contains("command: /plan register")
                || message.contains("command: /plan web register")
                || message.contains("command: /plan webuser register");
        if (block) {
            return Result.DENY;
        }
        return null;
    }

    @Override
    public Result filter(LogEvent event) {
        String message = event.getMessage().toString().toLowerCase();
        return filter(message);
    }

    @Override
    public Result filter(Logger arg0, Level arg1, Marker arg2, String arg3, Object... arg4) {
        return filter(arg3);
    }

    @Override
    public Result filter(Logger arg0, Level arg1, Marker arg2, Object arg3, Throwable arg4) {
        return null;
    }

    @Override
    public Result filter(Logger arg0, Level arg1, Marker arg2, Message arg3, Throwable arg4) {
        return filter(arg3.toString());
    }

    @Override
    public Result getOnMatch() {
        return null;
    }

    @Override
    public Result getOnMismatch() {
        return null;
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String s, Object o) {
        return filter(s);
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String s, Object o, Object o1) {
        return filter(s);
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String s, Object o, Object o1, Object o2) {
        return filter(s);
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String s, Object o, Object o1, Object o2, Object o3) {
        return filter(s);
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String s, Object o, Object o1, Object o2, Object o3, Object o4) {
        return filter(s);
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String s, Object o, Object o1, Object o2, Object o3, Object o4, Object o5) {
        return filter(s);
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String s, Object o, Object o1, Object o2, Object o3, Object o4, Object o5, Object o6) {
        return filter(s);
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String s, Object o, Object o1, Object o2, Object o3, Object o4, Object o5, Object o6, Object o7) {
        return filter(s);
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String s, Object o, Object o1, Object o2, Object o3, Object o4, Object o5, Object o6, Object o7, Object o8) {
        return filter(s);
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String s, Object o, Object o1, Object o2, Object o3, Object o4, Object o5, Object o6, Object o7, Object o8, Object o9) {
        return filter(s);
    }

    @Override
    public State getState() {
        return null;
    }

    @Override
    public void initialize() {

    }

    @Override
    public void start() {
        start = true;
    }

    @Override
    public void stop() {
        start = false;
    }

    @Override
    public boolean isStarted() {
        return start;
    }

    @Override
    public boolean isStopped() {
        return !start;
    }
}
