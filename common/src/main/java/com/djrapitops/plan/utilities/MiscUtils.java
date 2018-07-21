package com.djrapitops.plan.utilities;

import com.djrapitops.plan.PlanHelper;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.locale.Locale;
import com.djrapitops.plan.system.settings.locale.Msg;
import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.command.CommandUtils;
import com.djrapitops.plugin.command.ISender;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

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

    public static int getTimeZoneOffsetHours() {
        if (Settings.USE_SERVER_TIME.isTrue()) {
            return -TimeZone.getDefault().getOffset(System.currentTimeMillis()) / (int) TimeAmount.HOUR.ms();
        }
        return 0;
    }

    /**
     * Get a players name that matches the given arguments or name of the sender.
     *
     * @param args   Arguments of command.
     * @param sender Sender of command
     * @return Player name.
     */
    public static String getPlayerName(String[] args, ISender sender) {
        return getPlayerName(args, sender, Permissions.INSPECT_OTHER);
    }

    /**
     * Used by the inspect command.
     *
     * @param args   Arguments of a command, must not be empty if console sender.
     * @param sender Command sender
     * @param perm   Permission to use when checking. No permission will notify user.
     * @return The name of the player (first argument or sender)
     */
    public static String getPlayerName(String[] args, ISender sender, Permissions perm) {
        String playerName = "";
        boolean isConsole = !CommandUtils.isPlayer(sender);
        if (isConsole) {
            playerName = args[0];
        } else if (args.length > 0) {
            if (sender.hasPermission(perm.getPermission())) {
                playerName = args[0];
            } else if (args[0].equalsIgnoreCase(sender.getName())) {
                playerName = sender.getName();
            } else {
                sender.sendMessage(Locale.get(Msg.CMD_FAIL_NO_PERMISSION).toString());
            }
        } else {
            playerName = sender.getName();
        }
        return playerName;
    }

    /**
     * Get matching player names from the offline players.
     *
     * @param search Part of a name to search for.
     * @return Alphabetically sorted list of matching player names.
     */
    public static List<String> getMatchingPlayerNames(String search) {
        Database db = Database.getActive();
        List<String> matches = db.search().matchingPlayers(search);
        Collections.sort(matches);
        return matches;
    }

    public static void close(Closeable... close) {
        for (Closeable c : close) {
            if (c != null) {
                try {
                    c.close();
                } catch (IOException e) {
                    if (Settings.DEV_MODE.isTrue()) {
                        Log.warn("THIS ERROR IS ONLY LOGGED IN DEV MODE:");
                        Log.toLog(MiscUtils.class, e);
                    }
                }
            }
        }
    }

    public static void close(AutoCloseable... close) {
        for (AutoCloseable c : close) {
            if (c != null) {
                try {
                    c.close();
                } catch (Exception e) {
                    if (Settings.DEV_MODE.isTrue()) {
                        Log.warn("THIS ERROR IS ONLY LOGGED IN DEV MODE:");
                        Log.toLog(MiscUtils.class, e);
                    }
                }
            }
        }
    }

    public static String getPlanVersion() {
        return PlanHelper.getInstance().getVersion();
    }
}
