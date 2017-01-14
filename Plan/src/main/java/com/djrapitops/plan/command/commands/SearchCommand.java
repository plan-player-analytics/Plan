package com.djrapitops.plan.command.commands;

import com.djrapitops.plan.Phrase;
import com.djrapitops.plan.Plan;
import com.djrapitops.plan.command.CommandType;
import com.djrapitops.plan.command.SubCommand;
import com.djrapitops.plan.data.cache.InspectCacheHandler;
import com.djrapitops.plan.utilities.MiscUtils;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

/**
 *
 * @author Rsl1122
 */
public class SearchCommand extends SubCommand {

    private final Plan plugin;
    private InspectCacheHandler inspectCache;

    /**
     * Class Constructor.
     *
     * @param plugin Current instance of Plan
     */
    public SearchCommand(Plan plugin) {
        super("search", "plan.search", "Search for player", CommandType.CONSOLE_WITH_ARGUMENTS, "<Part of Playername");
        this.plugin = plugin;
        inspectCache = plugin.getInspectCache();
    }

    /**
     * Subcommand search.
     *
     * Searches database for matching playernames and caches matching PlayerData
     * to InspectCache. Shows all links to matching players data.
     *
     * @param sender
     * @param cmd
     * @param commandLabel
     * @param args Part of a Players name
     * @return true in all cases.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (args.length != 1) {
            sender.sendMessage(Phrase.COMMAND_REQUIRES_ARGUMENTS_ONE.toString());
        }

        ChatColor oColor = Phrase.COLOR_MAIN.color();
        ChatColor tColor = Phrase.COLOR_SEC.color();
        ChatColor hColor = Phrase.COLOR_TER.color();

        Set<OfflinePlayer> matches = MiscUtils.getMatchingDisplaynames(args[0]);
        Set<UUID> uuids = new HashSet<>();
        for (OfflinePlayer match : matches) {
            UUID uuid = match.getUniqueId();
            if (plugin.getDB().wasSeenBefore(uuid)) {
                uuids.add(uuid);
                inspectCache.cache(uuid);
            }
        }

        FileConfiguration config = plugin.getConfig();
        final boolean useAlternativeIP = config.getBoolean("Settings.WebServer.ShowAlternativeServerIP");
        final int port = config.getInt("Settings.WebServer.Port");
        final String alternativeIP = config.getString("Settings.WebServer.AlternativeIP").replaceAll("%port%", "" + port);
        int configValue = config.getInt("Settings.Cache.InspectCache.ClearFromInspectCacheAfterXMinutes");
        if (configValue <= 0) {
            configValue = 4;
        }
        final int available = configValue;

        // Header
        sender.sendMessage(hColor + Phrase.ARROWS_RIGHT.toString() + oColor + " Player Analytics - Search results for: " + args[0]);
        // Results
        if (uuids.isEmpty()) {
            sender.sendMessage(tColor + " " + Phrase.BALL.toString() + oColor + "No results for " + tColor + Arrays.toString(args) + oColor + ".");
        } else {
            for (OfflinePlayer match : matches) {
                if (!uuids.contains(match.getUniqueId())) {
                    continue;
                }
                String name = match.getName();
                sender.sendMessage(tColor + " Matching player: " + hColor + name);
                // Link
                String url = "http://" + (useAlternativeIP ? alternativeIP : plugin.getServer().getIp() + ":" + port)
                        + "/player/" + name;
                String message = tColor + " " + Phrase.BALL.toString() + oColor + " Link: " + hColor;
                boolean console = !(sender instanceof Player);
                if (console) {
                    sender.sendMessage(message + url);
                } else {
                    sender.sendMessage(message);
                    Player player = (Player) sender;
                    Bukkit.getServer().dispatchCommand(
                            Bukkit.getConsoleSender(),
                            "tellraw " + player.getName() + " [\"\",{\"text\":\"Click Me\",\"underlined\":true,"
                            + "\"clickEvent\":{\"action\":\"open_url\",\"value\":\"" + url + "\"}}]");
                }
            }
        }
        sender.sendMessage(tColor + "   Results will be available for " + hColor + available + tColor + " minutes.");
        // Footer
        sender.sendMessage(hColor + Phrase.ARROWS_RIGHT.toString());
        return true;
    }
}
