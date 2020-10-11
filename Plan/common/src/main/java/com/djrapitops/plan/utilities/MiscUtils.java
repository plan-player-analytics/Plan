/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.utilities;

import com.djrapitops.plan.settings.Permissions;
import com.djrapitops.plugin.command.CommandUtils;
import com.djrapitops.plugin.command.Sender;

import java.util.concurrent.TimeUnit;

/**
 * Utility method class containing various static methods.
 *
 * @author Rsl1122
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
        return getPlayerName(args, sender, Permissions.PLAYER_OTHER);
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

    public static long now() {
        return System.currentTimeMillis();
    }

    public static long dayAgo() {
        return now() - TimeUnit.DAYS.toMillis(1L);
    }

    public static long weekAgo() {
        return now() - (TimeUnit.DAYS.toMillis(7L));
    }

    public static long monthAgo() {
        return now() - (TimeUnit.DAYS.toMillis(30L));
    }
}
