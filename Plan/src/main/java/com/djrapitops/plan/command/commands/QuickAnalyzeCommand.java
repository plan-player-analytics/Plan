package main.java.com.djrapitops.plan.command.commands;

import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Permissions;
import main.java.com.djrapitops.plan.Phrase;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.command.CommandType;
import main.java.com.djrapitops.plan.command.SubCommand;
import main.java.com.djrapitops.plan.data.cache.AnalysisCacheHandler;
import main.java.com.djrapitops.plan.ui.TextUI;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

/**
 * This subcommand is used to run the analysis and to view some of the data in
 * game.
 *
 * @author Rsl1122
 * @since 3.0.0
 */
public class QuickAnalyzeCommand extends SubCommand {

    private Plan plugin;
    private AnalysisCacheHandler analysisCache;

    /**
     * Subcommand Constructor.
     *
     * @param plugin Current instance of Plan
     */
    public QuickAnalyzeCommand(Plan plugin) {
        super("qanalyze, qanalyse, qanalysis", Permissions.QUICK_ANALYZE, Phrase.CMD_USG_QANALYZE.parse(), CommandType.CONSOLE, "");
        this.plugin = plugin;
        analysisCache = plugin.getAnalysisCache();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        sender.sendMessage(Phrase.GRABBING_DATA_MESSAGE + "");
        if (!analysisCache.isCached()) {
            int bootAnID = plugin.getBootAnalysisTaskID();
            if (bootAnID != -1) {
                plugin.getServer().getScheduler().cancelTask(bootAnID);
            }
            analysisCache.updateCache();
        } else if (MiscUtils.getTime() - analysisCache.getData().getRefreshDate() > 60000) {
            analysisCache.updateCache();
        }

        BukkitTask analysisMessageSenderTask = new BukkitRunnable() {
            private int timesrun = 0;

            @Override
            public void run() {
                timesrun++;
                if (analysisCache.isCached()) {
                    sender.sendMessage(Phrase.CMD_ANALYZE_HEADER + "");
                    sender.sendMessage(TextUI.getAnalysisMessages());
                    sender.sendMessage(Phrase.CMD_FOOTER + "");
                    this.cancel();
                }
                if (timesrun > 10) {
                    Log.debug("Command Timeout Message, QuickAnalyze.");
                    sender.sendMessage(Phrase.COMMAND_TIMEOUT.parse("Analysis"));
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 1 * 20, 5 * 20);
        return true;
    }
}
