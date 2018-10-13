package com.djrapitops.plan.utilities;

import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plugin.command.CommandUtils;
import com.djrapitops.plugin.command.Sender;

import java.io.Closeable;
import java.io.IOException;

/**
 * Utility method class containing various static methods.
 *
 * @author Rsl1122
 * @since 2.0.0
 */
public class MiscUtils {

    /**
     * Constructor used to hide the public constructor
     */
    private MiscUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Get a players name that matches the given arguments or name of the sender.
     *
     * @param args   Arguments of command.
     * @param sender Sender of command
     * @return Player name.
     */
    public static String getPlayerName(String[] args, Sender sender) {
        return getPlayerName(args, sender, Permissions.INSPECT_OTHER);
    }

    /**
     * Used by the inspect command.
     *
     * @param args   Arguments of a command, must not be empty if console sender.
     * @param sender Command sender
     * @param perm   Permission to use when checking.
     * @return The name of the player (first argument or sender) or null if sender has no permission.
     */
    public static String getPlayerName(String[] args, Sender sender, Permissions perm) {
        String playerName;
        boolean isConsole = !CommandUtils.isPlayer(sender);
        if (isConsole) {
            playerName = args[0];
        } else if (args.length > 0) {
            if (sender.hasPermission(perm.getPermission())) {
                playerName = args[0];
            } else if (args[0].equalsIgnoreCase(sender.getName())) {
                playerName = sender.getName();
            } else {
                return null;
            }
        } else {
            playerName = sender.getName();
        }
        return playerName;
    }

    public static void close(Closeable... close) {
        for (Closeable c : close) {
            if (c != null) {
                try {
                    c.close();
                } catch (IOException ignored) {
                    // Closing exceptions are ignored.
                }
            }
        }
    }

    public static void close(AutoCloseable... close) {
        for (AutoCloseable c : close) {
            if (c != null) {
                try {
                    c.close();
                } catch (Exception ignore) {
                    // Closing exceptions are ignored.
                }
            }
        }
    }
}
