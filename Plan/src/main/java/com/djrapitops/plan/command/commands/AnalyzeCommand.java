package com.djrapitops.plan.command.commands;

import com.djrapitops.plan.Phrase;
import com.djrapitops.plan.Plan;
import com.djrapitops.plan.command.CommandType;
import com.djrapitops.plan.command.SubCommand;
import com.djrapitops.plan.data.cache.AnalysisCacheHandler;
import java.util.Date;
import main.java.com.djrapitops.plan.Settings;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
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
     * Subcommand Constructor.
     *
     * @param plugin Current instance of Plan
     */
    public AnalyzeCommand(Plan plugin) {
        super("analyze, analyse, analysis", "plan.analyze", Phrase.CMD_USG_ANALYZE.parse(), CommandType.CONSOLE, "");
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
        if (!Settings.WEBSERVER_ENABLED.isTrue()) {
            if (!Settings.SHOW_ALTERNATIVE_IP.isTrue()) {
                sender.sendMessage(Phrase.ERROR_WEBSERVER_OFF_ANALYSIS.toString());
                return true;
            } else {
                sendAnalysisMessage(sender);
                return true;
            }
        }
        sender.sendMessage(Phrase.GRABBING_DATA_MESSAGE+"");
        if (!analysisCache.isCached()) {
            int bootAnID = plugin.getBootAnalysisTaskID();
            if (bootAnID != -1) {
                plugin.getServer().getScheduler().cancelTask(bootAnID);
            }
            analysisCache.updateCache();
        } else if (new Date().getTime() - analysisCache.getData().getRefreshDate() > 60000) {
            analysisCache.updateCache();
        }

        (new BukkitRunnable() {
            
            private int timesrun = 0;
            
            @Override
            public void run() {
                timesrun++;
                if (analysisCache.isCached()) {
                    sendAnalysisMessage(sender);
                    this.cancel();
                }
                if (timesrun > 45) {
                    sender.sendMessage(Phrase.COMMAND_TIMEOUT.parse("Analysis"));
                }
            }
        }).runTaskTimer(plugin, 1 * 20, 5 * 20);
        return true;
    }

    /**
     * Used to send the message after /plan analysis.
     * @param sender Command sender.
     * @throws CommandException
     */
    public void sendAnalysisMessage(CommandSender sender) throws CommandException {
        final boolean useAlternativeIP = Settings.SHOW_ALTERNATIVE_IP.isTrue();
        final int port = Settings.WEBSERVER_PORT.getNumber();
        final String alternativeIP = Settings.ALTERNATIVE_IP.toString().replaceAll("%port%", "" + port);
        
        sender.sendMessage(Phrase.CMD_ANALYZE_HEADER+"");
        // Link
        String url = "http://" + (useAlternativeIP ? alternativeIP : plugin.getServer().getIp() + ":" + port)
                + "/server";
        String message = Phrase.CMD_LINK+"";
        boolean console = !(sender instanceof Player);
        if (console) {
            sender.sendMessage(message + url);
        } else {
            sender.sendMessage(message);
            Player player = (Player) sender;
            Bukkit.getServer().dispatchCommand(
                    Bukkit.getConsoleSender(),
                    "tellraw " + player.getName() + " [\"\",{\"text\":\""+Phrase.CMD_CLICK_ME+"\",\"underlined\":true,"
                    + "\"clickEvent\":{\"action\":\"open_url\",\"value\":\"" + url + "\"}}]");
        }
        sender.sendMessage(Phrase.CMD_FOOTER+"");
    }
}
