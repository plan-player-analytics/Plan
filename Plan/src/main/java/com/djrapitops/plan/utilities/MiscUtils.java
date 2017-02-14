package main.java.com.djrapitops.plan.utilities;

import main.java.com.djrapitops.plan.Phrase;
import main.java.com.djrapitops.plan.Plan;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import static org.bukkit.plugin.java.JavaPlugin.getPlugin;

/**
 *
 * @author Rsl1122
 */
public class MiscUtils {

    /**
     * Checks the version and returns response String.
     *
     * @return String informing about status of plugins version.
     */
    public static String checkVersion() {
        Plan plugin = getPlugin(Plan.class);
        String cVersion;
        String lineWithVersion;
        try {
            URL githubUrl = new URL("https://raw.githubusercontent.com/Rsl1122/Plan-PlayerAnalytics/master/Plan/src/main/resources/plugin.yml");
            lineWithVersion = "";
            Scanner websiteScanner = new Scanner(githubUrl.openStream());
            while (websiteScanner.hasNextLine()) {
                String line = websiteScanner.nextLine();
                if (line.toLowerCase().contains("version")) {
                    lineWithVersion = line;
                    break;
                }
            }
            String versionString = lineWithVersion.split(": ")[1];
            int newestVersionNumber = FormatUtils.parseVersionNumber(versionString);
            cVersion = plugin.getDescription().getVersion();
            int currentVersionNumber = FormatUtils.parseVersionNumber(cVersion);
            if (newestVersionNumber > currentVersionNumber) {
                return Phrase.VERSION_NEW_AVAILABLE.parse(versionString);
            } else {
                return Phrase.VERSION_LATEST + "";
            }
        } catch (IOException | NumberFormatException e) {
            plugin.logError(Phrase.VERSION_CHECK_ERROR + "");
        }
        return Phrase.VERSION_FAIL + "";
    }

    /**
     * Used by the inspect command.
     *
     * @param args Arguments of the inspect command
     * @param sender Command sender
     * @return The name of the player searched for, if the arguments are empty
     * player's own name is returned.
     */
    public static String getPlayerDisplayname(String[] args, CommandSender sender) {
        String playerName = "";
        Plan plugin = getPlugin(Plan.class);
        boolean isConsole = !(sender instanceof Player);
        if (args.length > 0) {
            if (sender.hasPermission("plan.inspect.other")
                    || isConsole) {
                playerName = args[0];
            } else if (args[0].toLowerCase().equals(sender.getName().toLowerCase())) {
                playerName = sender.getName();
            } else {
                sender.sendMessage(Phrase.COMMAND_NO_PERMISSION.toString());
            }
        } else {
            try {
                Player player = plugin.getServer().getPlayer(UUIDFetcher.getUUIDOf(sender.getName()));
                playerName = player.getName();
            } catch (Exception e) {
                plugin.logError(Phrase.ERROR_CONSOLE_PLAYER.parse(Arrays.toString(args),isConsole+""));
            }
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
        players.parallelStream()
                .filter((OfflinePlayer player) -> (player.getName().toLowerCase().contains(search.toLowerCase())))
                .forEach((OfflinePlayer player) -> {
                    matches.add(player);
                });
        return matches;
    }

    public static boolean isOnSameDay(Date first, Date second) {
        Date startOfFirst = getStartOfDate(first);
        Date startOfSecond = getStartOfDate(second);
        return (startOfFirst != startOfSecond);
    }

    public static Date getStartOfDate(Date date) {
        Date startOfDate = new Date(date.getTime() - (date.getTime() % 86400000));
        return startOfDate;
    }
}
