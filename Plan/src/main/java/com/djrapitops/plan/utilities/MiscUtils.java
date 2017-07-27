package main.java.com.djrapitops.plan.utilities;

import com.djrapitops.plugin.command.CommandUtils;
import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.utilities.player.Fetch;
import com.djrapitops.plugin.utilities.player.IOfflinePlayer;
import main.java.com.djrapitops.plan.Permissions;
import main.java.com.djrapitops.plan.Phrase;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility method class containing various static methods.
 *
 * @author Rsl1122
 * @since 2.0.0
 */
public class MiscUtils {

    /**
     * Used to get the current time as milliseconds.
     *
     * @return Epoch ms.
     */
    public static long getTime() {
        return System.currentTimeMillis();
    }

    /**
     * @param args
     * @param sender
     * @return
     */
    public static String getPlayerName(String[] args, ISender sender) {
        return getPlayerName(args, sender, Permissions.INSPECT_OTHER);
    }

    /**
     * Used by the inspect command.
     *
     * @param args   Arguments of a command, must not be empty if console sender.
     * @param sender Command sender
     * @param perm
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
            } else if (args[0].toLowerCase().equals(sender.getName().toLowerCase())) {
                playerName = sender.getName();
            } else {
                sender.sendMessage(Phrase.COMMAND_NO_PERMISSION.toString());
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
        final String searchFor = search.toLowerCase();
        List<String> matches = Fetch.getIOfflinePlayers().stream()
                .map(IOfflinePlayer::getName)
                .filter(name -> name.toLowerCase().contains(searchFor))
                .collect(Collectors.toList());
        Collections.sort(matches);
        return matches;
    }

    public static <T> List<T> flatMap(Collection<List<T>> coll) {
        return coll.stream().flatMap(Collection::stream).collect(Collectors.toList());
    }

    public static void close(Closeable... close) {
        for (Closeable c : close) {
            if (c != null) {
                try {
                    c.close();
                } catch (IOException ex) {
                }
            }
        }
    }

    public static void close(AutoCloseable... close) {
        for (AutoCloseable c : close) {
            if (c != null) {
                try {
                    c.close();
                } catch (Exception ex) {
                }
            }
        }
    }
}
