package main.java.com.djrapitops.plan.command.commands;

import java.util.UUID;
import main.java.com.djrapitops.plan.Permissions;
import main.java.com.djrapitops.plan.Phrase;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.command.CommandType;
import main.java.com.djrapitops.plan.command.SubCommand;
import main.java.com.djrapitops.plan.data.cache.InspectCacheHandler;
import main.java.com.djrapitops.plan.ui.TextUI;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import main.java.com.djrapitops.plan.utilities.UUIDFetcher;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

/**
 * This command is used to cache UserData to InspectCache and to view some of
 * the data in game.
 *
 * @author Rsl1122
 * @since 3.0.0
 */
public class QuickInspectCommand extends SubCommand {

    private Plan plugin;
    private InspectCacheHandler inspectCache;

    /**
     * Class Constructor.
     *
     * @param plugin Current instance of Plan
     */
    public QuickInspectCommand(Plan plugin) {
        super("qinspect", Permissions.QUICK_INSPECT, Phrase.CMD_USG_QINSPECT + "", CommandType.CONSOLE_WITH_ARGUMENTS, Phrase.ARG_PLAYER + "");

        this.plugin = plugin;
        inspectCache = plugin.getInspectCache();
    }

    /**
     * Subcommand qinspect (QuickInspect).
     *
     * Adds player's data from DataCache/DB to the InspectCache
     *
     * @param sender args is empty, can not be Console.
     * @param cmd
     * @param commandLabel
     * @param args Player's name or nothing - if empty sender's name is used.
     * @return true in all cases.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        String playerName = MiscUtils.getPlayerName(args, sender, Permissions.QUICK_INSPECT_OTHER);
        BukkitTask inspectTask = (new BukkitRunnable() {
            @Override
            public void run() {
                UUID uuid;
                try {
                    uuid = UUIDFetcher.getUUIDOf(playerName);
                    if (uuid == null) {
                        throw new Exception("Username doesn't exist.");
                    }
                } catch (Exception e) {
                    sender.sendMessage(Phrase.USERNAME_NOT_VALID.toString());
                    this.cancel();
                    return;
                }
                OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);
                if (!p.hasPlayedBefore()) {
                    sender.sendMessage(Phrase.USERNAME_NOT_SEEN.toString());
                    this.cancel();
                    return;
                }
                if (!plugin.getDB().wasSeenBefore(uuid)) {
                    sender.sendMessage(Phrase.USERNAME_NOT_KNOWN.toString());
                    this.cancel();
                    return;
                }
                sender.sendMessage(Phrase.GRABBING_DATA_MESSAGE + "");
                inspectCache.cache(uuid);
                int configValue = Settings.CLEAR_INSPECT_CACHE.getNumber();
                if (configValue <= 0) {
                    configValue = 4;
                }
                final int available = configValue;
                BukkitTask inspectMessageSenderTask = (new BukkitRunnable() {
                    private int timesrun = 0;

                    @Override
                    public void run() {
                        timesrun++;
                        if (inspectCache.isCached(uuid)) {
                            sender.sendMessage(Phrase.CMD_INSPECT_HEADER + playerName);
                            sender.sendMessage(TextUI.getInspectMessages(uuid));
                            sender.sendMessage(Phrase.CMD_FOOTER + "");
                            this.cancel();
                        }
                        if (timesrun > 10) {
                            sender.sendMessage(Phrase.COMMAND_TIMEOUT.parse("Qinspect"));
                            this.cancel();
                        }
                    }
                }).runTaskTimer(plugin, 1 * 20, 5 * 20);
            }
        }).runTaskAsynchronously(plugin);
        return true;
    }
}
