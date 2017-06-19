package main.java.com.djrapitops.plan.utilities;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Permissions;
import main.java.com.djrapitops.plan.Phrase;
import main.java.com.djrapitops.plan.Plan;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
     *
     * @param args
     * @param sender
     * @return
     */
    public static String getPlayerName(String[] args, CommandSender sender) {
        return getPlayerName(args, sender, Permissions.INSPECT_OTHER);
    }

    /**
     * Used by the inspect command.
     *
     * @param args Arguments of a command, must not be empty if console sender.
     * @param sender Command sender
     * @param perm
     * @return The name of the player (first argument or sender)
     */
    public static String getPlayerName(String[] args, CommandSender sender, Permissions perm) {
        String playerName = "";
        boolean isConsole = !(sender instanceof Player);
        if (isConsole) {
            playerName = args[0];
        } else if (args.length > 0) {
            if (perm.userHasThisPermission(sender)) {
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
     * Get matching playernames from the offlineplayers
     *
     * @param search Part of a name to search for.
     * @return Set of OfflinePlayers that match.
     */
    public static Set<OfflinePlayer> getMatchingDisplaynames(String search) {
        List<OfflinePlayer> players = new ArrayList<>();
        players.addAll(Arrays.asList(Bukkit.getOfflinePlayers()));
        Set<OfflinePlayer> matches = new HashSet<>();
        players.stream()
                .filter(player -> (player.getName().toLowerCase().contains(search.toLowerCase())))
                .forEach(player -> {
                    matches.add(player);
                });
        return matches;
    }
}
