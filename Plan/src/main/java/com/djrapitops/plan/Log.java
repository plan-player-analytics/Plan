package main.java.com.djrapitops.plan;

import java.util.Collection;

/**
 * This class manages the messages going to the Bukkit's Logger.
 *
 * @author Rsl1122
 * @since 3.0.0
 */
public class Log {

    /**
     * Logs the message to the console as INFO.
     *
     * @param message "Message" will show up as [INFO][Plan]: Message
     */
    public static void info(String message) {
        Plan.getPluginInstance().getPluginLogger().info(message);
    }

    public static void infoColor(String message) {
        Plan.getInstance().getPluginLogger().infoColor(message);
    }

    /**
     * Logs an error message to the console as ERROR.
     *
     * @param message "Message" will show up as [ERROR][Plan]: Message
     */
    public static void error(String message) {
        Plan.getInstance().getPluginLogger().error(message);
    }

    /**
     * Logs a debug message to the console as INFO if Settings.Debug is true.
     *
     * @param message "Message" will show up as [INFO][Plan]: [DEBUG] Message
     */
    public static void debug(String message) {
        Plan.getInstance().getPluginLogger().debug(message);
    }

    /**
     * Logs trace of caught Exception to Errors.txt and notifies on console.
     *
     * @param source Class name the exception was caught in.
     * @param e Throwable, eg NullPointerException
     */
    public static void toLog(String source, Throwable e) {
        Plan.getInstance().getPluginLogger().toLog(source, e);
    }

    /**
     * Logs multiple caught Errors to Errors.txt.
     *
     * @param source Class name the exception was caught in.
     * @param e Collection of Throwables, eg NullPointerException
     */
    public static void toLog(String source, Collection<Throwable> e) {
        Plan.getInstance().getPluginLogger().toLog(source, e);
    }

    /**
     * Logs a message to the a given file with a timestamp.
     *
     * @param message Message to log to Errors.txt [timestamp] Message
     * @param filename Name of the file to write to.
     */
    public static void toLog(String message, String filename) {
        Plan.getInstance().getPluginLogger().toLog(message, filename);
    }

    public static String getErrorsFilename() {
        return Plan.getInstance().getPluginLogger().getErrorsFilename();
    }
}
