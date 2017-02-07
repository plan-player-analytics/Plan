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
import main.java.com.djrapitops.plan.Settings;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Rsl1122
 */
public class SearchCommand extends SubCommand {

    private final Plan plugin;
    private final InspectCacheHandler inspectCache;

    /**
     * Class Constructor.
     *
     * @param plugin Current instance of Plan
     */
    public SearchCommand(Plan plugin) {
        super("search", "plan.search", Phrase.CMD_USG_SEARCH+"", CommandType.CONSOLE_WITH_ARGUMENTS, Phrase.ARG_SEARCH+"");
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
        if (!Settings.WEBSERVER_ENABLED.isTrue()) {
            sender.sendMessage(Phrase.ERROR_WEBSERVER_OFF_ANALYSIS.toString());
            return true;
        }
        if (args.length != 1) {
            sender.sendMessage(Phrase.COMMAND_REQUIRES_ARGUMENTS_ONE.toString());
            return true;
        }

        sender.sendMessage(Phrase.GRABBING_DATA_MESSAGE + "");
        Set<OfflinePlayer> matches = MiscUtils.getMatchingDisplaynames(args[0]);
        Set<UUID> uuids = new HashSet<>();
        for (OfflinePlayer match : matches) {
            UUID uuid = match.getUniqueId();
            if (plugin.getDB().wasSeenBefore(uuid)) {
                uuids.add(uuid);
                inspectCache.cache(uuid);
            }
        }

        final boolean useAlternativeIP = Settings.SHOW_ALTERNATIVE_IP.isTrue();
        final int port = Settings.WEBSERVER_PORT.getNumber();
        final String alternativeIP = Settings.ALTERNATIVE_IP.toString().replaceAll("%port%", "" + port);
        int configValue = Settings.CLEAR_INSPECT_CACHE.getNumber();
        if (configValue <= 0) {
            configValue = 4;
        }
        final int available = configValue;

        sender.sendMessage(Phrase.CMD_SEARCH_HEADER + args[0]);
        // Results
        if (uuids.isEmpty()) {
            sender.sendMessage(Phrase.CMD_NO_RESULTS.parse(Arrays.toString(args)));
        } else {
            for (OfflinePlayer match : matches) {
                if (!uuids.contains(match.getUniqueId())) {
                    continue;
                }
                String name = match.getName();
                sender.sendMessage(Phrase.CMD_MATCH + name);
                // Link
                String url = "http://" + (useAlternativeIP ? alternativeIP : plugin.getServer().getIp() + ":" + port)
                        + "/player/" + name;
                String message = Phrase.CMD_LINK+"";
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
        sender.sendMessage(Phrase.CMD_RESULTS_AVAILABLE.parse(available+""));
        sender.sendMessage(Phrase.CMD_FOOTER+"");
        return true;
    }
}
