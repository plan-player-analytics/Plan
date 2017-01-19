package com.djrapitops.plan.command.commands;

import com.djrapitops.plan.Phrase;
import com.djrapitops.plan.Plan;
import com.djrapitops.plan.command.CommandType;
import com.djrapitops.plan.command.SubCommand;
import com.djrapitops.plan.data.cache.AnalysisCacheHandler;
import java.util.Date;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author Rsl1122
 */
public class AnalyzeCommand extends SubCommand {

    private Plan plugin;
    private AnalysisCacheHandler analysisCache;

    /**
     * Class Constructor.
     *
     * @param plugin Current instance of Plan
     */
    public AnalyzeCommand(Plan plugin) {
        super("analyze", "plan.analyze", "View the Server Analysis", CommandType.CONSOLE, "");
        this.plugin = plugin;
        analysisCache = plugin.getAnalysisCache();
    }

    /**
     * Subcommand analyze.
     *
     * Updates AnalysisCache if last refresh was over 60 seconds ago and sends
     * player the link that views cache.
     *
     * @param sender
     * @param cmd
     * @param commandLabel
     * @param args
     * @return true in all cases.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        FileConfiguration config = plugin.getConfig();
        if (!config.getBoolean("Settings.WebServer.Enabled")) {
            if (!config.getBoolean("Settings.WebServer.ShowAlternativeServerIP")) {
                sender.sendMessage(Phrase.ERROR_WEBSERVER_OFF_ANALYSIS.toString());
                return true;
            } else {
                sendAnalysisMessage(sender, config);
                return true;
            }
        }
        if (!analysisCache.isCached()) {
            analysisCache.updateCache();
        } else if (new Date().getTime() - analysisCache.getData().getRefreshDate() > 60000) {
            analysisCache.updateCache();
        }

        (new BukkitRunnable() {
            @Override
            public void run() {
                if (analysisCache.isCached()) {
                    sendAnalysisMessage(sender, config);
                    this.cancel();
                }
            }
        }).runTaskTimer(plugin, 1 * 20, 5 * 20);
        return true;
    }

    public void sendAnalysisMessage(CommandSender sender, FileConfiguration config) throws CommandException {
        ChatColor oColor = Phrase.COLOR_MAIN.color();
        ChatColor tColor = Phrase.COLOR_SEC.color();
        ChatColor hColor = Phrase.COLOR_TER.color();
        final boolean useAlternativeIP = config.getBoolean("Settings.WebServer.ShowAlternativeServerIP");
        final int port = config.getInt("Settings.WebServer.Port");
        final String alternativeIP = config.getString("Settings.WebServer.AlternativeIP").replaceAll("%port%", "" + port);
        // Header
        sender.sendMessage(hColor + Phrase.ARROWS_RIGHT.toString() + oColor
                + " Player Analytics - Analysis results");
        // Link
        String url = "http://" + (useAlternativeIP ? alternativeIP : plugin.getServer().getIp() + ":" + port)
                + "/server";
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
        // Footer
        sender.sendMessage(hColor + Phrase.ARROWS_RIGHT.toString());
    }
}
