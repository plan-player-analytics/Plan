package main.java.com.djrapitops.plan.utilities;

import com.djrapitops.plugin.command.ISender;
import main.java.com.djrapitops.plan.Log;

/**
 * Class containing static check methods with message sending capabilities if
 * the check is false.
 *
 * @author Rsl1122
 */
public class Check {

    /**
     * Constructor used to hide the public constructor
     */
    private Check() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * If check is false, send message.
     *
     * @param condition Condition.
     * @param message   Message to send if Condition is false
     * @return Condition
     */
    public static boolean isTrue(boolean condition, String message) {
        if (!condition) {
            Log.infoColor(message);
        }
        return condition;
    }

    /**
     * If check is false, send message to sender.
     *
     * @param condition Condition.
     * @param message   Message to send if Condition is false
     * @param sender    Sender to send message to
     * @return Condition
     */
    public static boolean isTrue(boolean condition, String message, ISender sender) {
        if (!condition) {
            sender.sendMessage(message);
        }
        return condition;
    }

    /**
     * If check is false, send error message.
     *
     * @param condition Condition.
     * @param message   Message to send if Condition is false
     * @return Condition
     */
    public static boolean ErrorIfFalse(boolean condition, String message) {
        if (!condition) {
            Log.error(message);
        }
        return condition;
    }
}
