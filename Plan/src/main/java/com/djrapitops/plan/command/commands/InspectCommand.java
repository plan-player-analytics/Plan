package com.djrapitops.plan.command.commands;

import com.djrapitops.plan.Phrase;
import com.djrapitops.plan.Plan;
import com.djrapitops.plan.utilities.UUIDFetcher;
import com.djrapitops.plan.command.CommandType;
import com.djrapitops.plan.command.SubCommand;

import java.util.Date;
import com.djrapitops.plan.data.cache.InspectCacheHandler;
import com.djrapitops.plan.utilities.FormatUtils;
import com.djrapitops.plan.utilities.MiscUtils;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import static org.bukkit.Bukkit.getOfflinePlayer;
import org.bukkit.scheduler.BukkitRunnable;

public class InspectCommand extends SubCommand {

    private Plan plugin;
    private InspectCacheHandler inspectCache;

    public InspectCommand(Plan plugin) {
        super("inspect", "plan.inspect", "Inspect data /plan <player>", CommandType.CONSOLE_WITH_ARGUMENTS);

        this.plugin = plugin;
        inspectCache = plugin.getInspectCache();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        String playerName = MiscUtils.getPlayerDisplayname(args, sender);

        UUID uuid;
        try {
            uuid = UUIDFetcher.getUUIDOf(playerName);
            if (uuid == null) {
                throw new Exception("Username doesn't exist.");
            }
        } catch (Exception e) {
            sender.sendMessage(Phrase.USERNAME_NOT_VALID.toString());
            return true;
        }
        OfflinePlayer p = getOfflinePlayer(uuid);
        if (!p.hasPlayedBefore()) {
            sender.sendMessage(Phrase.USERNAME_NOT_SEEN.toString());
            return true;
        }
        if (!plugin.getDB().wasSeenBefore(uuid)) {
            sender.sendMessage(Phrase.USERNAME_NOT_KNOWN.toString());
            return true;
        }

        Date refreshDate = new Date();
        inspectCache.cache(uuid);
        ChatColor operatorColor = Phrase.COLOR_MAIN.color();
        ChatColor textColor = Phrase.COLOR_SEC.color();
        (new BukkitRunnable() {
            @Override
            public void run() {
                if (inspectCache.getCache().containsKey(uuid)) {
                    sender.sendMessage(textColor + "-- [" + operatorColor + "PLAN - Inspect results: " + playerName + " - took " + FormatUtils.formatTimeAmountSinceDate(refreshDate, new Date()) + textColor + "] --");
                    sender.sendMessage(operatorColor + "Link: " + textColor
                            + "http://" + plugin.getServer().getIp() + ":" + plugin.getConfig().getString("WebServer.Port"
                            ) + "/player/" + playerName);
                    sender.sendMessage(textColor+"Results will be available for 5 minutes.");
                    sender.sendMessage(textColor + "-- o --");
                    this.cancel();
                }
            }
        }).runTaskTimer(plugin, 1 * 20, 5 * 20);
        return true;
    }
}
