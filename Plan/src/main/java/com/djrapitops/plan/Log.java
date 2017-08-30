package main.java.com.djrapitops.plan;

import com.djrapitops.plugin.utilities.log.DebugInfo;
import com.djrapitops.plugin.utilities.log.PluginLog;
import main.java.com.djrapitops.plan.utilities.MiscUtils;

import java.util.Collection;

/**
 * This class manages the messages going to the Console Logger.
 * <p>
 * Methods of Abstract Plugin Framework log utility are used.
 *
 * @author Rsl1122
 * @since 3.0.0
 */
public class Log {

    /**
     * Constructor used to hide the public constructor
     */
    private Log() {
        throw new IllegalStateException("Utility Class");
    }

    /**
     * Logs the message to the console as INFO.
     *
     * @param message "Message" will show up as [INFO][Plan]: Message
     */
    public static void info(String message) {
        getPluginLogger().info(message);
    }

    /**
     * Sends a message to the console with the ChatColors.
     *
     * @param message Message to send.
     */
    public static void infoColor(String message) {
        getPluginLogger().infoColor(message);
    }

    /**
     * Logs an error message to the console as ERROR.
     *
     * @param message "Message" will show up as [ERROR][Plan]: Message
     */
    public static void error(String message) {
        getPluginLogger().error(message);
    }

    /**
     * Logs a debug message to the console as INFO if Settings.Debug is true.
     *
     * @param message "Message" will show up as [INFO][Plan]: [DEBUG] Message
     */
    public static void debug(String message) {
        getPluginLogger().debug(message);
    }


    /**
     * Used for logging larger debug complexes.
     *
     * @param task    complex this debug message is a part of.
     * @param message Single message to add to the debug log.
     * @return full debug complex so far.
     */
    public static DebugInfo debug(String task, String message) {
        return getDebug(task).addLine(message, MiscUtils.getTime());
    }

    /**
     * Used for logging larger debug complexes.
     *
     * @param task     complex this debug message is a part of.
     * @param messages All messages to add to the debug log.
     * @return full debug complex so far.
     */
    public static DebugInfo debug(String task, String... messages) {
        DebugInfo debug = getDebug(task);
        long time = MiscUtils.getTime();
        for (String message : messages) {
            debug.addLine(message, time);
        }
        return debug;
    }

    /**
     * Used for logging larger debug complexes.
     *
     * @param task complex to get
     * @return full debug complex so far.
     */
    public static DebugInfo getDebug(String task) {
        return getPluginLogger().getDebug(task);
    }

    /**
     * Logs the full debug complex to the debug log.
     *
     * @param task complex to log.
     */
    public static void logDebug(String task) {
        getDebug(task).toLog();
    }

    /**
     * Logs the full debug complex to the debug log with an execution time.
     *
     * @param task complex to log.
     * @param time execution time.
     */
    public static void logDebug(String task, long time) {
        getDebug(task).toLog(time);
    }

    /**
     * Logs trace of caught Exception to Errors.txt and notifies on console.
     *
     * @param source Class name the exception was caught in.
     * @param e      {@code Throwable}, eg NullPointerException
     */
    public static void toLog(String source, Throwable e) {
        getPluginLogger().toLog(source, e);
    }

    /**
     * Logs multiple caught Errors to Errors.txt.
     *
     * @param source Class name the exception was caught in.
     * @param e      Collection of {@code Throwable}, eg NullPointerException
     */
    public static void toLog(String source, Collection<Throwable> e) {
        Plan.getInstance().getPluginLogger().toLog(source, e);
    }

    private static PluginLog getPluginLogger() {
        return MiscUtils.getIPlan().getPluginLogger();
    }

    public static void logStackTrace(Throwable e) {
        error(e.toString());
        for (StackTraceElement stackTraceElement : e.getStackTrace()) {
            error("  " + stackTraceElement);
        }
        Throwable cause = e.getCause();
        if (cause != null) {
            logCause(cause);
        }
    }

    private static void logCause(Throwable e) {
        error("caused by: " + e.toString());
        for (StackTraceElement stackTraceElement : e.getStackTrace()) {
            error("  " + stackTraceElement);
        }
        Throwable cause = e.getCause();
        if (cause != null) {
            logCause(cause);
        }
    }
}
