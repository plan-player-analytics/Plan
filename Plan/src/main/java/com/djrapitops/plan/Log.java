package main.java.com.djrapitops.plan;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import main.java.com.djrapitops.plan.utilities.FormatUtils;
import main.java.com.djrapitops.plan.utilities.MiscUtils;

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
        Plan instance = Plan.getInstance();
        if (instance != null) {
            instance.getLogger().info(message);
        }
    }

    /**
     * Logs an error message to the console as ERROR.
     *
     * @param message "Message" will show up as [ERROR][Plan]: Message
     */
    public static void error(String message) {
        Plan instance = Plan.getInstance();
        if (instance != null) {
            instance.getLogger().severe(message);
        }
    }

    /**
     * Logs a debug message to the console as INFO if Settings.Debug is true.
     *
     * @param message "Message" will show up as [INFO][Plan]: [DEBUG] Message
     */
    public static void debug(String message) {
        if (Settings.DEBUG.isTrue()) {
            info("[DEBUG] " + message);
        }
    }

    /**
     * Logs trace of caught Exception to Errors.txt and notifies on console.
     *
     * @param source Class name the exception was caught in.
     * @param e Throwable, eg NullPointerException
     */
    public static void toLog(String source, Throwable e) {
        error(Phrase.ERROR_LOGGED.parse(e.toString()));
        toLog(source + " Caught " + e);
        for (StackTraceElement x : e.getStackTrace()) {
            toLog("  " + x);
        }
        toLog("");
    }

    /**
     * Logs multiple caught Errors to Errors.txt.
     *
     * @param source Class name the exception was caught in.
     * @param e Collection of Throwables, eg NullPointerException
     */
    public static void toLog(String source, Collection<Throwable> e) {
        for (Throwable ex : e) {
            toLog(source, ex);
        }
    }

    /**
     * Logs a message to the Errors.txt with a timestamp.
     *
     * @param message Message to log to Errors.txt [timestamp] Message
     */
    public static void toLog(String message) {
        Plan plan = Plan.getInstance();
        if (plan == null) {
            return;
        }
        File folder = plan.getDataFolder();
        if (!folder.exists()) {
            folder.mkdir();
        }
        File log = new File(folder, "Errors.txt");
        try {
            if (!log.exists()) {
                log.createNewFile();
            }
            FileWriter fw = new FileWriter(log, true);
            try (PrintWriter pw = new PrintWriter(fw)) {
                String timestamp = FormatUtils.formatTimeStamp(MiscUtils.getTime());
                pw.println("[" + timestamp + "] " + message);
                pw.flush();
            }
        } catch (IOException e) {
            plan.getLogger().severe("Failed to create Errors.txt file");
        }
    }
}
