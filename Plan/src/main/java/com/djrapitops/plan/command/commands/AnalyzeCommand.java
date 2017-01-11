package com.djrapitops.plan.command.commands;

import com.djrapitops.plan.Phrase;
import com.djrapitops.plan.Plan;
import com.djrapitops.plan.command.CommandType;
import com.djrapitops.plan.command.SubCommand;
import com.djrapitops.plan.data.cache.AnalysisCacheHandler;
import com.djrapitops.plan.utilities.FormatUtils;
import java.util.Date;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

public class AnalyzeCommand extends SubCommand {

    private Plan plugin;
    private AnalysisCacheHandler analysisCache;

    public AnalyzeCommand(Plan plugin) {
        super("analyze", "plan.analyze", "Analyze data of all players /plan analyze", CommandType.CONSOLE);
        this.plugin = plugin;
        analysisCache = plugin.getAnalysisCache();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (!analysisCache.isCached()) {
            analysisCache.updateCache();
        } else if (new Date().getTime() - analysisCache.getData().getRefreshDate() > 60 * 5) {
            analysisCache.updateCache();
        }
        ChatColor operatorColor = Phrase.COLOR_MAIN.color();
        ChatColor textColor = Phrase.COLOR_SEC.color();
        (new BukkitRunnable() {
            @Override
            public void run() {
                if (analysisCache.isCached()) {
                    sender.sendMessage(textColor + "-- [" + operatorColor + "PLAN - Analysis results, refreshed "
                            + FormatUtils.formatTimeAmountSinceString("" + analysisCache.getData().getRefreshDate(), new Date()) + " ago:" + textColor + "] --");
                    sender.sendMessage(operatorColor + "Link: " + textColor
                            + "http://" + plugin.getServer().getIp() + ":" + plugin.getConfig().getString("WebServer.Port"
                            ) + "/server");
                    sender.sendMessage(textColor + "-- o --");
                    this.cancel();
                }
            }
        }).runTaskTimer(plugin, 1 * 20, 5 * 20);
        return true;
    }
}
