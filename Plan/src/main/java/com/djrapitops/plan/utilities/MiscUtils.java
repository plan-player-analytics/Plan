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
        try {
            Plan plugin = Plan.getInstance();
            String cVersion = plugin.getDescription().getVersion();
            String gitVersion = getGitVersion();
            return checkVersion(cVersion, gitVersion);
        } catch (IOException | NumberFormatException e) {
            Log.error(Phrase.VERSION_CHECK_ERROR + "");
        }
        return Phrase.VERSION_FAIL + "";
    }

    private static String getGitVersion() throws IOException {
        URL githubUrl = new URL("https://raw.githubusercontent.com/Rsl1122/Plan-PlayerAnalytics/master/Plan/src/main/resources/plugin.yml");
        String lineWithVersion = "";
        Scanner websiteScanner = new Scanner(githubUrl.openStream());
        while (websiteScanner.hasNextLine()) {
            String line = websiteScanner.nextLine();
            if (line.toLowerCase().contains("version")) {
                lineWithVersion = line;
                break;
            }
        }
        return lineWithVersion.split(": ")[1];
    }

    /**
     *
     * @param currentVersion
     * @param gitVersion
     * @return
     * @throws NumberFormatException
     */
    public static String checkVersion(String currentVersion, String gitVersion) throws NumberFormatException {
        int newestVersionNumber = FormatUtils.parseVersionNumber(gitVersion);
        int currentVersionNumber = FormatUtils.parseVersionNumber(currentVersion);
        if (newestVersionNumber > currentVersionNumber) {
            return Phrase.VERSION_NEW_AVAILABLE.parse(gitVersion);
        } else {
            return Phrase.VERSION_LATEST + "";
        }
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
        players.parallelStream()
                .filter(player -> (player.getName().toLowerCase().contains(search.toLowerCase())))
                .forEach(player -> {
                    matches.add(player);
                });
        return matches;
    }
}
