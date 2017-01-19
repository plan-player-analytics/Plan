package com.djrapitops.plan.command.commands;

import com.djrapitops.plan.Phrase;
import com.djrapitops.plan.Plan;
import com.djrapitops.plan.PlanLiteHook;
import com.djrapitops.plan.utilities.UUIDFetcher;
import com.djrapitops.plan.command.CommandType;
import com.djrapitops.plan.command.SubCommand;

import java.util.Date;
import com.djrapitops.plan.data.cache.InspectCacheHandler;
import com.djrapitops.plan.utilities.MiscUtils;
import java.util.UUID;
import org.bukkit.Bukkit;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import static org.bukkit.Bukkit.getOfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author Rsl1122
 */
public class InspectCommand extends SubCommand {

    private Plan plugin;
    private InspectCacheHandler inspectCache;

    /**
     * Class Constructor.
     *
     * @param plugin Current instance of Plan
     */
    public InspectCommand(Plan plugin) {
        super("inspect", "plan.inspect", "Inspect Player's Data", CommandType.CONSOLE_WITH_ARGUMENTS, "<player>");

        this.plugin = plugin;
        inspectCache = plugin.getInspectCache();
    }

    /**
     * Subcommand inspect.
     *
     * Adds player's data from DataCache/DB to the InspectCache for amount of
     * time specified in the config, and clears the data from Cache with a timer
     * task.
     *
     * @param sender
     * @param cmd
     * @param commandLabel
     * @param args Player's name or nothing - if empty sender's name is used.
     * @return true in all cases.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        FileConfiguration config = plugin.getConfig();
        if (!config.getBoolean("Settings.WebServer.Enabled")) {
            if (!config.getBoolean("Settings.WebServer.ShowAlternativeServerIP")) {
                PlanLiteHook planLiteHook = plugin.getPlanLiteHook();
                if (config.getBoolean("Settings.PlanLite.UseAsAlternativeUI") && planLiteHook.isEnabled()) {
                    sender.sendMessage(ChatColor.YELLOW + "[Plan] Passing to PlanLite..");
                    planLiteHook.passCommand(sender, cmd, commandLabel, args);
                } else {
                    sender.sendMessage(Phrase.ERROR_WEBSERVER_OFF_INSPECT.toString());
                }
                return true;
            }
        }
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

        inspectCache.cache(uuid);
        ChatColor oColor = Phrase.COLOR_MAIN.color();
        ChatColor tColor = Phrase.COLOR_SEC.color();
        ChatColor hColor = Phrase.COLOR_TER.color();

        final boolean useAlternativeIP = config.getBoolean("Settings.WebServer.ShowAlternativeServerIP");
        final int port = config.getInt("Settings.WebServer.Port");
        final String alternativeIP = config.getString("Settings.WebServer.AlternativeIP").replaceAll("%port%", "" + port);
        int configValue = config.getInt("Settings.Cache.InspectCache.ClearFromInspectCacheAfterXMinutes");
        if (configValue <= 0) {
            configValue = 4;
        }
        final int available = configValue;
        (new BukkitRunnable() {
            @Override
            public void run() {
                if (inspectCache.getCache().containsKey(uuid)) {
                    // Header
                    sender.sendMessage(hColor + Phrase.ARROWS_RIGHT.toString() + oColor
                            + " Player Analytics - Inspect results: " + oColor + playerName);
                    // Link
                    String url = "http://" + (useAlternativeIP ? alternativeIP : plugin.getServer().getIp() + ":" + port)
                            + "/player/" + playerName;
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

                    sender.sendMessage(tColor + "   Results will be available for " + hColor + available + tColor + " minutes.");
                    // Footer
                    sender.sendMessage(hColor + Phrase.ARROWS_RIGHT.toString());
                    this.cancel();
                }
            }
        }).runTaskTimer(plugin, 1 * 20, 5 * 20);
        return true;
    }
}
