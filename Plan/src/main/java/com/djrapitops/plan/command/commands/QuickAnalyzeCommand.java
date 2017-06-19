package main.java.com.djrapitops.plan.command.commands;

import com.djrapitops.javaplugin.command.CommandType;
import com.djrapitops.javaplugin.command.SubCommand;
import com.djrapitops.javaplugin.task.RslBukkitRunnable;
import com.djrapitops.javaplugin.task.RslTask;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Permissions;
import main.java.com.djrapitops.plan.Phrase;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.cache.AnalysisCacheHandler;
import main.java.com.djrapitops.plan.ui.TextUI;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 * This subcommand is used to run the analysis and to view some of the data in
 * game.
 *
 * @author Rsl1122
 * @since 3.0.0
 */
public class QuickAnalyzeCommand extends SubCommand {

    private final Plan plugin;
    private final AnalysisCacheHandler analysisCache;

    /**
     * Subcommand Constructor.
     *
     * @param plugin Current instance of Plan
     */
    public QuickAnalyzeCommand(Plan plugin) {
        super("qanalyze, qanalyse, qanalysis", CommandType.CONSOLE, Permissions.QUICK_ANALYZE.getPermission(), Phrase.CMD_USG_QANALYZE.parse());
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

        RslTask analysisMessageSenderTask = new RslBukkitRunnable<Plan>("QanalysisMessageSenderTask") {
            private int timesrun = 0;

            @Override
            public void run() {
                timesrun++;
                if (analysisCache.isCached() && !analysisCache.isAnalysisBeingRun()) {
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
        }.runTaskTimer(1 * 20, 5 * 20);
        return true;
    }
}
